package com.heslin.postopia.common.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
public class KafkaConsumerProperties {
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.max-poll-records}")
    private int maxPollRecords;
    @Value("${spring.kafka.consumer.fetch-min-size}")
    private int fetchMinSize;
    @Value("${spring.kafka.consumer.fetch-max-wait}")
    private int fetchMaxWait;
    @Value("${spring.kafka.consumer.bootstrap-servers}")
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
