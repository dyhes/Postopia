package com.heslin.postopia.kafka;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.kafka.consumer")
@Data
@Validated
public class KafkaConsumerProperties {
    // 基础配置
    private String groupId;
    private int maxPollRecords;
    private int fetchMinSize;
    private int fetchMaxWait;
    private String bootstrapServers;

    public Map<String, Object> buildConfigs() {
        return new HashMap<>(Map.of(
        ConsumerConfig.GROUP_ID_CONFIG, groupId,
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords,
        ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize,
        ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWait,
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers
        ));
    }
}
