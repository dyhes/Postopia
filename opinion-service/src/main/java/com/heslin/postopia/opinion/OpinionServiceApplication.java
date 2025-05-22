package com.heslin.postopia.opinion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.heslin.postopia.common")
@SpringBootApplication
@EnableDiscoveryClient
public class OpinionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpinionServiceApplication.class, args);
    }
}
