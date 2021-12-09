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
}
