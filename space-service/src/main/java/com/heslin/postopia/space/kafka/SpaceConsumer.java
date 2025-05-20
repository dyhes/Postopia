package com.heslin.postopia.space.kafka;

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
public class SpaceConsumer {
    private final KafkaService kafkaService;

    @Autowired
    public SpaceConsumer(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @KafkaListener(topics = "space", containerFactory = "batchLIFactory")
    @Transactional
    protected void processSpaceOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new SpaceDiff());
            diff.updateDiff(record.value());
        });
        kafkaService.executeBatchDiffOperations(mp, "spaces");
    }
}
