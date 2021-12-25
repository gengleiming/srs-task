package com.intellif.vesionbook.srstask.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ServerConfig {
    @Value("${stream.srs.host:192.168.18.151}")
    private String srsHost;
    @Value("${spring.application.name}")
    private String serviceId;
    @Value("${stream.output.host:192.168.18.151}")
    private String outputHost;
    @Value("${stream.output.port:8080}")
    private String httpOutputPort;

    @Value("${stream.with.javacv:0}")
    private String useJavacv;

    @Value("${stream.pool.limit:100}")
    private Integer streamPoolLimit;

    @Value("${stream.ffmpeg.log:warning}")
    private String ffmpegLogLevel;

    @Value("${stream.support.type:rtsp,gb28181}")
    private String supportType;

    // 3分钟之内推流和拉流的客户端总数，不能超过100个
    @Value("clients.limit.3min:100")
    private String clientsLimit;
}
