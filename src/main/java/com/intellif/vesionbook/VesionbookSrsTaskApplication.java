package com.intellif.vesionbook;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication
@EnableApolloConfig({"application", "vesionbook.common"})
public class VesionbookSrsTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(VesionbookSrsTaskApplication.class, args);
    }

}
