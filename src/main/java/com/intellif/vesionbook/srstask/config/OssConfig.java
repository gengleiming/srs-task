package com.intellif.vesionbook.srstask.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OssConfig {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.accessKey.id}")
    private String accessKeyId;
    @Value("${oss.accessKey.secret}")
    private String accessKeySecret;
    @Value("${oss.bucket.name}")
    private String bucketName;
    @Value("${oss.bucket.root}")
    private String bucketRoot;
    @Value("${oss.region.id}")
    private String regionId;
    @Value("${oss.sts.roleArn}")
    private String stsRoleArn;
    @Value("${oss.sts.roleSessionName}")
    private String stsRoleSessionName;
    @Value("${oss.sts.durationSeconds}")
    private Long stsDurationSeconds;
    @Value("${oss.zone.id:UTC}")
    private String ossZoneId;
}
