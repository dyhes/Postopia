package com.heslin.postopia.user.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class OSConfig {

    @Value("${postopia.os.apikey}")
    private String cloudUrl;

    @Bean
    Cloudinary cloudinary() {
        return new Cloudinary(cloudUrl);
    }
}
