package com.intellif.vesionbook.srstask.helper;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.intellif.vesionbook.srstask.config.OssConfig;
import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import com.intellif.vesionbook.srstask.model.vo.base.BaseResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            log.error("您的Bucket不存在，Bucket：{}", ossConfig.getBucketName());
//            ossClient.createBucket(ossConfig.getBucketName());
        }
        return ossClient;
    }

    public boolean uploadFile(String path, String objectName, String downloadName) {
        OSS client = getClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossConfig.getBucketName(), objectName, new File(path));
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentDisposition("attachment; filename=\"" + downloadName + "\"");
            putObjectRequest.setMetadata(meta);
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

    public OSS getOssStsCredentialsClient() {
        DefaultProfile profile = DefaultProfile.getProfile(ossConfig.getRegionId(), ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setDurationSeconds(ossConfig.getStsDurationSeconds());
        request.setRoleArn(ossConfig.getStsRoleArn());
        request.setRoleSessionName(ossConfig.getStsRoleSessionName());

        try {
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            if(credentials == null) {
                return null;
            }

            String stsAccessKeyId = credentials.getAccessKeyId();
            String stsAccessKeySecret = credentials.getAccessKeySecret();
            String stsSecurityToken = credentials.getSecurityToken();

            return new OSSClientBuilder().build(ossConfig.getEndpoint(), stsAccessKeyId,
                    stsAccessKeySecret, stsSecurityToken);
        } catch (ClientException e) {
            log.error("get sts credentials error. error code: {}, error msg: {}, request id: {}",
                    e.getErrCode(), e.getErrMsg(), e.getRequestId());
        } finally {
            client.shutdown();
        }
        return null;
    }

    public BaseResponseVo<String> getOssUrl(String objectName, OSS ossStsCredentialsClient) {

        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(ossConfig.getStsDurationSeconds());
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        try {
            boolean exist = ossStsCredentialsClient.doesObjectExist(ossConfig.getBucketName(), objectName);
            if(!exist) {
                log.error("object name not exist. bucket name: {}, object name: {}", ossConfig.getBucketName(), objectName);
                return BaseResponseVo.error(ReturnCodeEnum.ERROR_OSS_OBJECT_NAME_NOT_EXIST);

            }
            URL url = ossStsCredentialsClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, date);
            return BaseResponseVo.ok(url.toString());
        } catch (Exception e) {
            log.error("generate pre signed url error. bucket name: {}, object name: {}, expire date: {}",
                    ossConfig.getBucketName(), objectName, date);
            return BaseResponseVo.error(ReturnCodeEnum.ERROR_OSS_GENERATE_OSS_URL_FAILED);
        }
    }

}
