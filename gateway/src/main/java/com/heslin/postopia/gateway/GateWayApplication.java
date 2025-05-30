package com.heslin.postopia.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.heslin.postopia.jwt", "com.heslin.postopia.gateway"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class GateWayApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GateWayApplication.class, args);
    }
}
