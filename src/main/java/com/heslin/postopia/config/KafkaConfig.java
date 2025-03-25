package com.heslin.postopia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public KafkaListenerContainerFactory<?> batchFactory(DefaultKafkaConsumerFactory<Long, Integer> defaultKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, Integer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(defaultKafkaConsumerFactory);
        return factory;
    }

//    @Bean
//    public ConsumerFactory<Long, Integer> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        return new DefaultKafkaConsumerFactory<>(props);
//    }
}
