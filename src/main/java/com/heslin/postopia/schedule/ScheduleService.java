package com.heslin.postopia.schedule;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.jpa.model.Message;
import com.heslin.postopia.redis.RedisService;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.service.message.MessageService;
import com.heslin.postopia.service.post.PostService;
import com.heslin.postopia.util.PostopiaFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final MessageService messageService;
    private final RedisService redisService;
    private final CommentService commentService;
    private final PostService postService;

    @Autowired
    public ScheduleService(MessageService messageService, RedisService redisService, CommentService commentService, PostService postService) {
        this.messageService = messageService;
        this.redisService = redisService;
        this.commentService = commentService;
        this.postService = postService;
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

    @Scheduled(cron = "0 * * * * *")
    public void opinionMessageSender() {
        int page = 0;
        while (true) {
            var pageres = redisService.findAllOpinionAggregations(0);
            page++;
            if (pageres.getTotalPages() > page) {
                List<Long> commentIds = new ArrayList<>();
                List<Long> postIds = new ArrayList<>();
                pageres.stream().forEach(opinionAggregation -> {
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

                var messages = pageres.stream().map(aggregation -> {
                    StringBuilder messageContent = new StringBuilder();
                    String type, link, username, hint;
                    if (aggregation.getCommentId() != null) {
                        type = "评论";
                        link = PostopiaFormatter.formatComment(aggregation.getSpaceName(), aggregation.getPostId(), aggregation.getCommentId());
                        username = commentHintMap.get(aggregation.getCommentId()).username();
                        hint = commentHintMap.get(aggregation.getCommentId()).hint();
                    } else {
                        type = "帖子";
                        link = PostopiaFormatter.formatPost(aggregation.getSpaceName(), aggregation.getPostId());
                        username = postHintMap.get(aggregation.getPostId()).username();
                        hint = postHintMap.get(aggregation.getPostId()).hint();
                    }
                    buildMessage("赞同", aggregation.getPositiveCount(), aggregation.getPositiveUser(), messageContent);
                    if (aggregation.getPositiveCount() > 0 && aggregation.getNegativeCount() > 0) {
                        messageContent.append("，");
                    }
                    buildMessage("反对", aggregation.getNegativeCount(), aggregation.getNegativeUser(), messageContent);
                    messageContent.append(String.format("了你的 %s: %s...%s", type, hint, link));
                    return Message.builder().username(username).isRead(false).createdAt(Instant.now())
                    .content(messageContent.toString()).build();
                }).toList();
                messageService.saveAll(messages);
            } else {
                break;
            }
        }
    }
}
