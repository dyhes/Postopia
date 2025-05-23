package com.heslin.postopia.comment.service;

import com.heslin.postopia.comment.dto.CommentOpinionHint;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.redis.RedisService;
import com.heslin.postopia.common.redis.model.COAggragation;
import com.heslin.postopia.common.redis.repository.COAggregationRepository;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentScheduleService {
    private final RedisService redisService;
    private final KafkaService kafkaService;
    private final CommentService commentService;
    private final COAggregationRepository coAggregationRepository;

    @Autowired
    public CommentScheduleService(RedisService redisService, KafkaService kafkaService, CommentService commentService, COAggregationRepository coAggregationRepository) {
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.commentService = commentService;
        this.coAggregationRepository = coAggregationRepository;
    }

    public void batchMessageSender(List<String> keys) {
        List<COAggragation> aggregations = keys.stream().map(key -> coAggregationRepository.findById(Long.parseLong(key.split(":")[1])).orElseThrow()).toList();
        coAggregationRepository.deleteAll(aggregations);
        Map<Long, COAggragation> aggragationMap = aggregations.stream().collect(Collectors.toMap(COAggragation::getId, aggregation -> aggregation));
        List<CommentOpinionHint> hints = commentService.getOpinionHints(aggregations.stream().map(COAggragation::getId).toList());
        hints.forEach(commentOpinionHint -> {
            COAggragation aggregation = aggragationMap.get(commentOpinionHint.id());
            StringBuilder messageContent = new StringBuilder();
            String link = PostopiaFormatter.formatComment(aggregation.getSpaceId(), commentOpinionHint.postId(), commentOpinionHint.id());
            aggregation.buildMessage(messageContent);
            messageContent.append(String.format("了你的评论: %s %s", commentOpinionHint.content(), link));
            kafkaService.sendMessage(commentOpinionHint.userId(), messageContent.toString());
        });
    }

    @Scheduled(cron = "0 * * * * *")
    public void opinionMessageSender() {
        redisService.sendOpinionMessage(this::batchMessageSender, "co_aggregation:*");
    }
}
