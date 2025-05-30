package com.heslin.postopia.comment.kafka;

import com.heslin.postopia.comment.service.CommentService;
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
public class CommentConsumer {
    private final KafkaService kafkaService;
    private final CommentService commentService;

    @Autowired
    public CommentConsumer(KafkaService kafkaService, CommentService commentService) {
        this.kafkaService = kafkaService;
        this.commentService = commentService;
    }

    @KafkaListener(topics = "comment", containerFactory = "batchLIFactory")
    @Transactional
    protected void processCommentOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new CommentDiff());
            diff.updateDiff(record.value());
        });
        kafkaService.executeBatchDiffOperations(mp, "comments");
    }

    @KafkaListener(topics = "post_cascade", containerFactory = "batchLIFactory")
    @Transactional
    protected void processPostDelete(List<ConsumerRecord<Long, Integer>> records) {
        List<Long> ids = records.stream().map(ConsumerRecord::key).toList();
        System.out.println("post_cascade_in comment");
        System.out.println(ids);
        commentService.deleteCommentByPostIds(ids);
    }
}
