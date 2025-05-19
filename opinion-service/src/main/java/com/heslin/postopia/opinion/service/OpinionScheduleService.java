package com.heslin.postopia.opinion.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.opinion.redis.OpinionRedisService;
import com.heslin.postopia.common.redis.model.OpinionAggregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpinionScheduleService {
    private final OpinionRedisService opinionRedisService;
    private final KafkaService kafkaService;

    @Autowired
    public OpinionScheduleService(OpinionRedisService opinionRedisService, KafkaService kafkaService) {
        this.opinionRedisService = opinionRedisService;
        this.kafkaService = kafkaService;
    }

    private void buildMessage(String type, long count, String username, StringBuilder messageContent) {
        if (count > 0) {
            messageContent.append(PostopiaFormatter.formatUser(username));
            if (count > 1) {
                messageContent.append(String.format("等 %d 人", count));
            }
            messageContent.append(type);
        }
    }

    public void batchMessageSender(List<OpinionAggregation> opinionAggregations) {
        List<Long> commentIds = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        opinionAggregations.forEach(opinionAggregation -> {
            if (opinionAggregation.getCommentId() == null) {
                postIds.add(opinionAggregation.getPostId());
            } else {
                commentIds.add(opinionAggregation.getCommentId());
            }
        });
        List<AuthorHint> commentHints = commentService.getAuthorHints(commentIds);
        List<AuthorHint> postHints = postService.getAuthorHints(postIds);
        Map<Long, AuthorHint> commentHintMap = commentHints.stream().collect(Collectors.toMap(AuthorHint::id, hint-> hint));
        Map<Long, AuthorHint> postHintMap = postHints.stream().collect(Collectors.toMap(AuthorHint::id, hint-> hint));

        opinionAggregations.forEach(aggregation -> {
            StringBuilder messageContent = new StringBuilder();
            String type, link, hint;
            Long userId;
            if (aggregation.getCommentId() != null) {
                type = "评论";
                link = PostopiaFormatter.formatComment(aggregation.getSpaceId(), aggregation.getPostId(), aggregation.getCommentId());
                userId = aggregation
                hint = commentHintMap.get(aggregation.getCommentId()).hint();
            } else {
                type = "帖子";
                link = PostopiaFormatter.formatPost(aggregation.getSpaceId(), aggregation.getPostId());
                username = postHintMap.get(aggregation.);
                hint = postHintMap.get(aggregation.getPostId()).hint();
            }
            buildMessage("赞同", aggregation.getPositiveCount(), aggregation.getPositiveUser(), messageContent);
            if (aggregation.getPositiveCount() > 0 && aggregation.getNegativeCount() > 0) {
                messageContent.append("，");
            }
            buildMessage("反对", aggregation.getNegativeCount(), aggregation.getNegativeUser(), messageContent);
            messageContent.append(String.format("了你的 %s: %s...%s", type, hint, link));
            kafkaService.sendMessage(userId, messageContent.toString());
        }).toList();
    }

    @Scheduled(cron = "0 * * * * *")
    public void opinionMessageSender() {
        opinionRedisService.sendOpinionMessage(this::batchMessageSender);
    }
}
