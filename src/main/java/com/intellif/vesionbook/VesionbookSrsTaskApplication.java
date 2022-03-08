package com.intellif.vesionbook;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableApolloConfig
//@EnableApolloConfig({"local", "application"})
@EnableScheduling
@EnableFeignClients
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class VesionbookSrsTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesionbookSrsTaskApplication.class, args);
    }

}
