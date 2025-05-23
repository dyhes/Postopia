package com.heslin.postopia.opinion.kafka;

import com.heslin.postopia.opinion.service.OpinionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OpinionConsumer {
    private final OpinionService opinionService;

    @Autowired
    public OpinionConsumer(OpinionService opinionService) {
        this.opinionService = opinionService;
    }


    @KafkaListener(topics = "comment_cascade", containerFactory = "batchLIFactory")
    @Transactional
    protected void processCommentDelete(List<ConsumerRecord<Long, Integer>> records) {
        List<Long> ids = records.stream().map(ConsumerRecord::key).toList();
        System.out.println("post_cascade_in opinion");
        System.out.println(ids);
        opinionService.deleteCommentOpinionInBatch(ids);
    }

    @KafkaListener(topics = "post_cascade", containerFactory = "batchLIFactory")
    @Transactional
    protected void processPostDelete(List<ConsumerRecord<Long, Integer>> records) {
        List<Long> ids = records.stream().map(ConsumerRecord::key).toList();
        System.out.println("post_cascade_in opinion");
        System.out.println(ids);
        opinionService.deletePostOpinionInBatch(ids);
    }
}
