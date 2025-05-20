package com.heslin.postopia.vote.kafka;

import com.heslin.postopia.common.kafka.Diff;
import com.heslin.postopia.common.kafka.KafkaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Component
public class VoteConsumer {
    private final KafkaService kafkaService;

    @Autowired
    public VoteConsumer(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @KafkaListener(topics = "vote", containerFactory = "batchLIFactory")
    @Transactional
    protected void processVoteOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new VoteDiff());
            diff.updateDiff(record.value());
        });
        kafkaService.executeBatchDiffOperations(mp, "votes");
    }
}
