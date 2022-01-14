package com.intellif.vesionbook;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableApolloConfig({"local", "application"})
@EnableScheduling
@EnableFeignClients
public class VesionbookSrsTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesionbookSrsTaskApplication.class, args);
    }

}
