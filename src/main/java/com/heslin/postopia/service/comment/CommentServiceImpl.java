package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.model.opinion.CommentOpinion;
import com.heslin.postopia.jpa.repository.CommentRepository;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.redis.RedisService;
import com.heslin.postopia.service.opinion.OpinionService;
import com.heslin.postopia.util.PostopiaFormatter;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.min;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final OpinionService opinionService;
    private final KafkaService kafkaService;
    private final RedisService redisService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, OpinionService opinionService, KafkaService kafkaService, RedisService redisService) {
        this.commentRepository = commentRepository;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
        this.redisService = redisService;
    }

    @Transactional
    protected Comment createComment(User user, Post post, Space space, String content, Comment parent, String replyUser) {
        Comment comment =
        Comment.builder()
        .user(user)
        .post(post)
        .isPined(false)
        .content(content)
        .parent(parent)
        .build();
        comment = commentRepository.save(comment);
        kafkaService.sendToDocCreate("comment", comment.getId().toString(),
            new CommentDoc(
            comment.getId(),
            comment.getContent(),
            parent != null? parent.getId().toString() : null,
            post.getId().toString(),
            space.getName(),
            user.getUsername()));
        kafkaService.sendToPost(post.getId(), PostOperation.COMMENT_CREATED);
        StringBuilder messageContent = new StringBuilder();
        messageContent
        .append(PostopiaFormatter.formatUser(user.getUsername()))
        .append("回复了您：")
        .append(content, 0, min(content.length(), 20))
        .append("... %s".formatted(PostopiaFormatter.formatComment(space.getName(), post.getId(), comment.getId())));
        kafkaService.sendMessage(replyUser, messageContent.toString());
        return comment;
    }

    @Override
    @Transactional
    public Comment replyToPost(Post post, String content, User user, Space space, String replyUser) {
        return createComment(user, post, space, content, null, replyUser);
    }

    @Override
    @Transactional
    public Comment reply(Post post, Comment parent, String content, User user, Space space, String replyUser) {
        return createComment(user, post, space, content, parent, replyUser);
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id, Long postId, String spaceName) {
        boolean success = commentRepository.deleteComment(id) > 0;
        if (success) {
            kafkaService.sendToPost(postId, PostOperation.COMMENT_DELETED);
            kafkaService.sendToDocDelete("comment", id.toString(), spaceName);
        }
        return success;
    }

    @Override
    public Page<CommentSummary> getCommentsByUser(Long queryId, Long selfId, Pageable pageable) {
        if (Objects.equals(queryId, selfId)) {
            return commentRepository.findCommentsBySelf(selfId, pageable);
        } else {
            return commentRepository.findCommentsByUser(queryId, selfId, pageable);
        }
    }

    @Override
    @Transactional
    public Page<CommentInfo> getCommentsByPost(Long postId, Long userId, Pageable pageable) {
        Page<CommentInfo> top =  commentRepository.findByPostId(postId, userId, pageable);
        Map<Long, CommentInfo> mp = new HashMap<>();
        List<CommentInfo> flattenChildren = commentRepository.findChildrenByCommentIds(top.stream().map(commentInfo -> {
            mp.put(commentInfo.getId(), commentInfo);
            return commentInfo.getId();
        }).toList(), userId).stream().map(arr -> new CommentInfo(
                (Long) arr[0],(Long) arr[1], (String) arr[2],
                (Instant) arr[3],
                (String) arr[4], (String) arr[5], (String) arr[6],
                OpinionStatus.valueOf((String) arr[7]),
                (long) arr[8], (long) arr[9], (boolean) arr[10]
        )).toList();
        for (CommentInfo commentInfo : flattenChildren) {
            mp.put(commentInfo.getId(), commentInfo);
            CommentInfo parent = mp.get(commentInfo.getParentId());
            parent.getChildren().add(commentInfo);
        }
        return top;
    }

    @Override
    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable) {
        List<Boolean> statuses = opinionStatus == OpinionStatus.NIL ? List.of(true, false) : opinionStatus == OpinionStatus.POSITIVE ? List.of(true) : List.of(false);
        return opinionService.getCommentOpinionsByUser(id, statuses, pageable);
    }

    @Override
    public List<SearchedCommentInfo> getCommentInfosInSearch(List<Long> ids) {
        return commentRepository.getCommentInfosInSearch(ids);
    }

    @Override
    public void upsertCommentOpinion(User user, Long id, Long postId, String spaceName, boolean isPositive) {
        CommentOpinion postOpinion = new CommentOpinion();
        postOpinion.setUser(user);
        postOpinion.setComment(Comment.builder().id(id).build());
        postOpinion.setPositive(isPositive);
        boolean isInsert = opinionService.upsertOpinion(postOpinion);
        if (isInsert) {
            kafkaService.sendToComment(id, isPositive? CommentOperation.LIKED : CommentOperation.DISLIKED );
        } else {
            kafkaService.sendToComment(id, isPositive? CommentOperation.SWITCH_TO_LIKE : CommentOperation.SWITCH_TO_DISLIKE );
        }
        redisService.updateOpinionAggregation(spaceName, postId, id, user.getUsername(), isPositive);
    }

    @Override
    public boolean deleteCommentOpinion(User user, Long id, boolean isPositive) {
        boolean success = opinionService.deleteCommentOpinion(id, user.getId(), isPositive);
        if (success) {
            kafkaService.sendToComment(id, isPositive? CommentOperation.CANCEL_LIKE : CommentOperation.CANCEL_DISLIKE);
        }
        return success;
    }

    @Override
    public List<AuthorHint> getAuthorHints(List<Long> commentIds) {
        return commentRepository.getAuthorHints(commentIds);
    }

    @Override
    public boolean checkCommentPinStatus(Long commentId, boolean isPined) {
        return commentRepository.checkCommentPinStatus(commentId, isPined) == 0;
    }

    @Override
    public void updatePinStatus(Long commentId, boolean isPined) {
        commentRepository.updatePinStatus(commentId, isPined);
    }
}
