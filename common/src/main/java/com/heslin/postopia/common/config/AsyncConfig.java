package com.heslin.postopia.common.config;

import com.alibaba.cloud.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Types;
import feign.codec.Decoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Configuration
@EnableFeignClients
@EnableAsync
public class AsyncConfig {

    @Bean("feignAsyncExecutor")
    public Executor feignAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("FeignAsync-");
        executor.initialize();
        return executor;
    }

    @Bean
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        // Configure the converter if needed
        return converter;
    }

    @Bean
    public Decoder feignDecoder(MappingJackson2HttpMessageConverter converter) {
        return (response, type) -> {
            System.out.println("Thread in decoder: " + Thread.currentThread().getName());
            try {
                String responseBody = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
                System.out.println("Response body: " + responseBody);
                System.out.println("Response type: " + type);

                // Create a new response with the same body
                feign.Response newResponse = feign.Response.builder()
                .status(response.status())
                .headers(response.headers())
                .reason(response.reason())
                .request(response.request())
                .body(responseBody.getBytes(StandardCharsets.UTF_8))
                .build();

                // Handle CompletableFuture return type
                if (Types.getRawType(type).equals(CompletableFuture.class)) {
                    Type innerType = ((ParameterizedType) type).getActualTypeArguments()[0];
                    Object result = new SpringDecoder(() -> new HttpMessageConverters(converter))
                    .decode(newResponse, innerType);
                    return CompletableFuture.completedFuture(result);
                }

                // Regular decoding for non-CompletableFuture types
                return new SpringDecoder(() -> new HttpMessageConverters(converter))
                .decode(newResponse, type);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error decoding response", e);
            }
        };
    }
}