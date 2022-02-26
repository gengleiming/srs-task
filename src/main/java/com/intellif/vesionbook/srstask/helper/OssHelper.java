package com.intellif.vesionbook.srstask.helper;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.intellif.vesionbook.srstask.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class OssHelper {
    @Resource
    OssConfig ossConfig;

    public OSS getClient() {
        OSS ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());
        if (!ossClient.doesBucketExist(ossConfig.getBucketName())) {
            log.info("您的Bucket不存在，创建Bucket：{}", ossConfig.getBucketName());
            ossClient.createBucket(ossConfig.getBucketName());
        }
        return ossClient;
    }

    public boolean uploadFile(String path, String objectName) {
        OSS client = getClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, new File(path));
            PutObjectResult result = client.putObject(putObjectRequest);
            log.info("upload file path: {}, object name: {}, result: {}", path, objectName, result.getResponse());
            return true;
        } catch (OSSException oe) {
            log.error("Caught an OSSException. message: {}, code: {}, request id: {}, host id: {}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
            return false;
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    public AssumeRoleResponse.Credentials getSTSCredentials() {
        DefaultProfile profile = DefaultProfile.getProfile(ossConfig.getRegionId(), ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setDurationSeconds(ossConfig.getStsDurationSeconds());
        request.setRoleArn(ossConfig.getRoleArn());
        request.setRoleSessionName(ossConfig.getRoleSessionName());

        //发起请求，并得到响应。
        try {
            AssumeRoleResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            return response.getCredentials();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
        return null;
    }

    public String getOssUrl(String objectName, AssumeRoleResponse.Credentials credentials) {
        String ossUrl = ossConfig.getOssUrlPre() + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
        if (credentials == null) {
            return ossUrl;
        }

        String stsAccessKeyId = credentials.getAccessKeyId();
        String stsAccessKeySecret = credentials.getAccessKeySecret();
        String stsSecurityToken = credentials.getSecurityToken();
        String stsExpiration = credentials.getExpiration();

        LocalDateTime localDateTime = LocalDateTime.parse(stsExpiration);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());


        OSS ossClient = new OSSClientBuilder().build(ossConfig.getEndpoint(), stsAccessKeyId,
                stsAccessKeySecret, stsSecurityToken);

        try {
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, date);
            return url.toString();
        } catch (Exception e) {
            log.error("generate pre signed url error. bucket name: {}, object name: {}, expire date: {}",
                    ossConfig.getBucketName(), objectName, date);
        }finally {
            if(ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

}
