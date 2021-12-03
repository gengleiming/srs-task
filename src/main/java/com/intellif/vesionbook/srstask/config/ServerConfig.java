package com.intellif.vesionbook.srstask.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ServerConfig {
    @Value("${service.host:http://vesionbook-srs-task")
    private String serviceHost;
    @Value("${service.other.list:")
    private String serviceList;
}
