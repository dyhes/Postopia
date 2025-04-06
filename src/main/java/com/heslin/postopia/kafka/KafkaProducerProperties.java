package com.heslin.postopia.kafka;


import lombok.Data;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.kafka.producer")
@Data
public class KafkaProducerProperties {
    private Class<?> keySerializer;
    private int batchSize;
    private String compressionType;
    private int retries;
    private String bootstrapServers;
    private Map<String, Object> properties;

    public Map<String, Object> buildConfigs() {
        Map<String, Object> configs = new HashMap<>(properties);
        configs.putAll(
            Map.of(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer,
                ProducerConfig.BATCH_SIZE_CONFIG, batchSize,
                ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType,
                ProducerConfig.RETRIES_CONFIG, retries,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
            )
        );
        return configs;
    }
}
