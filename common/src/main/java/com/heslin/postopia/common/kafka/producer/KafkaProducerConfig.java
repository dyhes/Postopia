package com.heslin.postopia.common.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<Long, Integer> liProducerFactory(KafkaProducerProperties kafkaProducerProperties) {
        Map<String, Object> configs = kafkaProducerProperties.buildConfigs();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<Long, Integer> liKafkaTemplate(
    @Qualifier("liProducerFactory") ProducerFactory<Long, Integer> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, String> ssProducerFactory(KafkaProducerProperties kafkaProducerProperties) { // 注入自动配置的 KafkaProperties
        Map<String, Object> configs = kafkaProducerProperties.buildConfigs();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, String> ssKafkaTemplate(
    @Qualifier("ssProducerFactory") ProducerFactory<String, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<Long, String> lsProducerFactory(KafkaProducerProperties kafkaProducerProperties) { // 注入自动配置的 KafkaProperties
        Map<String, Object> configs = kafkaProducerProperties.buildConfigs();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<Long, String> lsKafkaTemplate(
    @Qualifier("lsProducerFactory") ProducerFactory<Long, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
