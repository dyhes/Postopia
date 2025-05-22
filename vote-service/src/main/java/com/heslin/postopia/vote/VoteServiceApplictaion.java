package com.heslin.postopia.vote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.heslin.postopia.common")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class VoteServiceApplictaion {
    public static void main(String[] args) {
        SpringApplication.run(VoteServiceApplictaion.class, args);
    }
}
