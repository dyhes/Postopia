package com.heslin.postopia.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Bean
    public ProducerFactory<Long, Integer> liProducerFactory(KafkaProducerProperties kafkaProducerProperties) { // 注入自动配置的 KafkaProperties
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

    @Bean
    public ConsumerFactory<Long, Integer> liConsumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> configs = kafkaConsumerProperties.buildConfigs();
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }



    @Bean
    public KafkaListenerContainerFactory<?> batchLIFactory(@Qualifier("liConsumerFactory") ConsumerFactory<Long, Integer> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, Integer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, String> ssConsumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> configs = kafkaConsumerProperties.buildConfigs();
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<?> batchSSFactory(@Qualifier("ssConsumerFactory") ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<?> ssFactory(@Qualifier("ssConsumerFactory") ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 不启用批量模式
        factory.setBatchListener(false);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<Long, String> lsConsumerFactory(KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> configs = kafkaConsumerProperties.buildConfigs();
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public KafkaListenerContainerFactory<?> batchLSFactory(@Qualifier("lsConsumerFactory") ConsumerFactory<Long, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量模式
        factory.setBatchListener(true);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
