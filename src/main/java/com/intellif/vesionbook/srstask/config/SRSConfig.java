package com.intellif.vesionbook.srstask.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class SRSConfig {
    @Value("${srs.host}")
    private String srsUrl;
}
