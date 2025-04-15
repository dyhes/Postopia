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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
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

    public void scheduleDeleteCommentVote(Long voteId, Long postId, String spaceName, String content, Instant endAt) {
        taskScheduler.schedule(
        () -> {
            Vote vote = voteRepository.findById(voteId).orElseThrow();
            System.out.println("scheduleDeleteCommentVote");
            List<VoteOpinion> voteOpinions = opinionRepository.findVoteOpinionsByVoteId(voteId);
            System.out.println("voteOpinions");
            voteOpinions.forEach(voteOpinion -> {
                System.out.println("username");
                System.out.println(voteOpinion.getUsername());
                System.out.println("altitude");
                System.out.println(voteOpinion.getAltitude());
            });
            List<Message> messages = new ArrayList<>();
            String tail;
            if (vote.isFulfilled()) {
                commentService.deleteComment(vote.getRelatedId(), postId, spaceName);
                tail = "的删除评论： %s 的投票已成功通过".formatted(content);
                messages.add(new Message(vote.getRelatedUser(), "您的评论：%s 已被投票删除".formatted(content)));
            } else {
                tail = "的删除评论：%s 的投票未通过, 赞成人数 %d，反对人数 %d, 所需最少投票人数 %d".formatted(content, vote.getPositiveCount(), vote.getNegativeCount(), vote.getThreshold());
            }
            messages.add(new Message(vote.getInitiator(), "您发起%s".formatted(tail)));
            voteOpinions.forEach(voteOpinion -> {
                messages.add(new Message(voteOpinion.getUsername(), "您%s%s".formatted(voteOpinion.getAltitude(),tail)));
            });
            messageService.batchSave(messages);
            voteRepository.delete(vote);
        },
        endAt
        );
    }

    public void schedulePinCommentVote(Long id, Instant endAt) {
        taskScheduler.schedule(
        () -> {
            System.out.println("scheduleCommentPinVote");
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
        System.out.println("commentIds");
        System.out.println(commentIds);
        System.out.println("postIds");
        System.out.println(postIds);
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
