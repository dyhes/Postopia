package com.heslin.postopia.schedule;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.jpa.model.Message;
import com.heslin.postopia.jpa.model.Vote;
import com.heslin.postopia.jpa.model.opinion.VoteOpinion;
import com.heslin.postopia.jpa.repository.OpinionRepository;
import com.heslin.postopia.jpa.repository.VoteRepository;
import com.heslin.postopia.redis.RedisService;
import com.heslin.postopia.redis.model.OpinionAggregation;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.service.message.MessageService;
import com.heslin.postopia.service.post.PostService;
import com.heslin.postopia.util.PostopiaFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final MessageService messageService;
    private final RedisService redisService;
    private final CommentService commentService;
    private final PostService postService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final VoteRepository voteRepository;
    private final OpinionRepository opinionRepository;

    @Autowired
    public ScheduleService(MessageService messageService, RedisService redisService, CommentService commentService, PostService postService, ThreadPoolTaskScheduler taskScheduler, VoteRepository voteRepository, OpinionRepository opinionRepository) {
        this.messageService = messageService;
        this.redisService = redisService;
        this.commentService = commentService;
        this.postService = postService;
        this.taskScheduler = taskScheduler;
        this.voteRepository = voteRepository;
        this.opinionRepository = opinionRepository;
    }

    private record VoteMessage(
        String voteAction,
        String relatedUserMessage
    ){}

    private void scheduledAction(Long voteId, String voteActionMessage, String relatedUserMessage, Function<Long, Void> voteAction) {
        Vote vote = voteRepository.findById(voteId).orElseThrow();
        List<VoteOpinion> voteOpinions = opinionRepository.findVoteOpinionsByVoteId(voteId);
        List<Message> messages = new ArrayList<>();
        String tail;
        if (vote.isFulfilled()) {
            voteAction.apply(vote.getRelatedId());
            tail = "的投票已成功通过";
            if (relatedUserMessage != null) {
                messages.add(new Message(vote.getRelatedUser(), relatedUserMessage));
            }
        } else {
            tail = "的投票未通过, 赞成人数 %d，反对人数 %d, 所需最少投票人数 %d".formatted(vote.getPositiveCount(), vote.getNegativeCount(), vote.getThreshold());
        }
        messages.add(new Message(vote.getInitiator(), "您发起的%s%s".formatted(voteActionMessage, tail)));
        voteOpinions.forEach(voteOpinion -> {
            messages.add(new Message(voteOpinion.getUsername(), "您%s的%s%s".formatted(voteOpinion.getAltitude(), voteActionMessage, tail)));
        });
        messageService.batchSave(messages);
        voteRepository.delete(vote);
    }

    public void scheduleUpdatePinStatusCommentVote(Long voteId, boolean isPined, Long commentId, Long postId, String spaceName, String commentContent, Instant endAt) {
        String commentMessage = "评论：%s%s".formatted(commentContent, PostopiaFormatter.formatComment(spaceName, postId, commentId));
        String voteActionMessage = "%s置顶%s".formatted(isPined ? "" : "取消", commentMessage);
        String relatedUserMessage = "您的%s已被投票%s置顶".formatted(commentMessage, isPined ? "" : "取消");
        taskScheduler.schedule(
        () -> {
            scheduledAction(voteId, voteActionMessage, relatedUserMessage, cId -> {
                commentService.updatePinStatus(commentId, isPined);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleDeleteCommentVote(Long voteId, Long postId, String spaceName, String content, Instant endAt) {
        taskScheduler.schedule(
        () -> {
            scheduledAction(voteId, "删除评论 %s".formatted(content), "您的评论：%s 已被投票删除".formatted(content), commentId -> {
                commentService.deleteComment(commentId, postId, spaceName);
                return null;
            });
        },
        endAt
        );
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

        var messages = opinionAggregations.stream().map(aggregation -> {
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
    }

    @Scheduled(cron = "0 * * * * *")
    public void opinionMessageSender() {
        redisService.sendOpinionMessage(this::batchMessageSender);
    }
}
