package com.heslin.postopia.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.serializer.UserIdSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializerConfig {
    private final UserIdSerializer userIdSerializer;

    @Autowired
    public SerializerConfig(UserIdSerializer userIdSerializer) {
        this.userIdSerializer = userIdSerializer;
    }

    @Bean
    @ConfigurationPropertiesBinding
    Module userIdModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(UserId.class, userIdSerializer);
        return module;
    }
}
