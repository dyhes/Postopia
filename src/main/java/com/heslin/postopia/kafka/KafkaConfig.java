package com.heslin.postopia.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public ProducerFactory<Long, Integer> integerProducerFactory(KafkaProducerProperties kafkaProducerProperties) { // 注入自动配置的 KafkaProperties
        Map<String, Object> configs = kafkaProducerProperties.buildConfigs();
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<Long, Integer> integerKafkaTemplate(
    @Qualifier("integerProducerFactory") ProducerFactory<Long, Integer> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<Long, String> stringProducerFactory(KafkaProducerProperties kafkaProducerProperties) { // 注入自动配置的 KafkaProperties
        Map<String, Object> configs = kafkaProducerProperties.buildConfigs();
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<Long, String> stringKafkaTemplate(
    @Qualifier("stringProducerFactory") ProducerFactory<Long, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<Long, Integer> integerConsumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> configs = kafkaConsumerProperties.buildConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }



    @Bean
    public KafkaListenerContainerFactory<?> batchIntegerFactory(@Qualifier("integerConsumerFactory") ConsumerFactory<Long, Integer> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, Integer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<Long, String> stringConsumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> configs = kafkaConsumerProperties.buildConfigs();
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<?> batchStringFactory(@Qualifier("stringConsumerFactory") ConsumerFactory<Long, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
