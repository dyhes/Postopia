package com.heslin.postopia.post.kafka;

import com.heslin.postopia.common.kafka.Diff;
import com.heslin.postopia.common.kafka.KafkaService;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class PostConsumer {
    private final KafkaService kafkaService;

    @Autowired
    public PostConsumer(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @KafkaListener(topics = "post", containerFactory = "batchLIFactory")
    @Transactional
    protected void processPostOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new PostDiff());
            diff.updateDiff(record.value());
        });
        kafkaService.executeBatchDiffOperations(mp, "posts");
    }
}
