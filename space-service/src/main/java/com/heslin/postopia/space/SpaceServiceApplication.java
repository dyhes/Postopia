package com.heslin.postopia.space;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpaceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpaceServiceApplication.class, args);
    }
}
