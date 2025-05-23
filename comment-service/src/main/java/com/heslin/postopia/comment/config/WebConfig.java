package com.heslin.postopia.comment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {
    @Bean
    WebClient webClient() {
        return WebClient.builder().baseUrl("https://api.siliconflow.cn/v1")
        .build();
    }
}
