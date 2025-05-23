package com.heslin.postopia.post.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.redis.RedisService;
import com.heslin.postopia.common.redis.model.POAggregation;
import com.heslin.postopia.common.redis.repository.POAggregationRepository;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.post.dto.PostOpinionHint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostScheduleService {
    private final RedisService redisService;
    private final KafkaService kafkaService;
    private final PostService postService;
    private final POAggregationRepository poAggregationRepository;

    @Autowired
    public PostScheduleService(RedisService redisService, KafkaService kafkaService, PostService postService, POAggregationRepository poAggregationRepository) {
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.postService = postService;
        this.poAggregationRepository = poAggregationRepository;
    }

    public void batchMessageSender(List<String> keys) {
        System.out.println("keys");
        System.out.println(keys);
        List<POAggregation> aggregations = keys.stream().map(key -> poAggregationRepository.findById(Long.parseLong(key.split(":")[1])).orElseThrow()).toList();
        poAggregationRepository.deleteAll(aggregations);
        Map<Long, POAggregation> aggragationMap = aggregations.stream().collect(Collectors.toMap(POAggregation::getId, aggregation -> aggregation));
        List<PostOpinionHint> hints = postService.getOpinionHints(aggregations.stream().map(POAggregation::getId).toList());
        hints.forEach(postOpinionHint -> {
            System.out.println("postOpinionHint");
            System.out.println(postOpinionHint);
            POAggregation aggregation = aggragationMap.get(postOpinionHint.id());
            StringBuilder messageContent = new StringBuilder();
            String link = PostopiaFormatter.formatPost(aggregation.getSpaceId(), postOpinionHint.id());
            aggregation.buildMessage(messageContent);
            messageContent.append(String.format("了你的帖子: %s %s", postOpinionHint.subject(), link));
            kafkaService.sendMessage(postOpinionHint.userId(), messageContent.toString());
        });
    }

    @Scheduled(cron = "0 * * * * *")
    public void opinionMessageSender() {
        System.out.println("Scheduled task in post");
        redisService.sendOpinionMessage(this::batchMessageSender, "po_aggregation:*");
    }
}
