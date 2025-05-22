package com.heslin.postopia.space;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.heslin.postopia.common")
@ComponentScan("com.heslin.postopia.space")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SpaceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpaceServiceApplication.class, args);
    }
}
