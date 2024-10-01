package com.heslin.postopia.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSConfig {

    @Value("${postopia.os.apikey}")
    private String cloudUrl;

    @Bean
    Cloudinary Cloudinary() {
        return new Cloudinary(cloudUrl);
    }
}
