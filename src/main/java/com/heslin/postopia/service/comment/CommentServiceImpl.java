package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.exception.ResourceNotFoundException;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.model.opinion.CommentOpinion;
import com.heslin.postopia.jpa.repository.CommentRepository;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.service.opinion.OpinionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final OpinionService opinionService;
    private final KafkaService kafkaService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, OpinionService opinionService, KafkaService kafkaService) {
        this.commentRepository = commentRepository;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
    }

    @Override
    @Transactional
    public Comment replyToPost(Post post, String content,@AuthenticationPrincipal User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment = commentRepository.save(comment);
        kafkaService.sendToPost(post.getId(), PostOperation.COMMENT_CREATED);
        return comment;
    }

    @Override
    @Transactional
    public void reply(Post post, Comment parent, String content,@AuthenticationPrincipal User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment.setParent(parent);
        commentRepository.save(comment);
        kafkaService.sendToPost(post.getId(), PostOperation.COMMENT_CREATED);
    }

    @Override
    public void deleteComment(Long id, Long postId, Long userId) {
        boolean succeed = commentRepository.deleteComment(id, postId, userId) == 1;
        if (succeed) {
            kafkaService.sendToPost(postId, PostOperation.COMMENT_DELETED);
        }
    }

    @Override
    public void checkAuthority(Long id, User user) {
        if (!Objects.equals(commentRepository.findUserIdById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found")), user.getId())) {
            throw new ForbiddenException("You are not the owner of this comment");
        }
    }

    @Override
    public void likeComment(Long id, User user) {
        addCommentOpinion(id, true, user);
    }

    @Override
    public void disLikeComment(Long id, User user) {
        addCommentOpinion(id, false, user);
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
                (Long) arr[0], (String) arr[1],
                (Instant) arr[2], (Long) arr[3],
                (String) arr[4], (String) arr[5],
                OpinionStatus.valueOf((String) arr[6]),
                (Long) arr[7], (long) arr[8], (long) arr[9]
        )).toList();
        for (CommentInfo commentInfo : flattenChildren) {
            mp.put(commentInfo.getId(), commentInfo);
            CommentInfo parent = mp.get(commentInfo.getParentId());
            parent.getChildren().add(commentInfo);
        }
        return top;
    }

    private void addCommentOpinion(Long id, boolean opinion, @AuthenticationPrincipal User user) {
        CommentOpinion postOpinion = new CommentOpinion();
        postOpinion.setUser(user);
        postOpinion.setComment(new Comment(id));
        postOpinion.setPositive(opinion);
        boolean isInsert = opinionService.upsertOpinion(postOpinion);
        if (isInsert) {
            kafkaService.sendToComment(id, opinion? CommentOperation.LIKED : CommentOperation.DISLIKED );
        } else {
            kafkaService.sendToComment(id, opinion? CommentOperation.SWITCH_TO_LIKE : CommentOperation.SWITCH_TO_DISLIKE );
        }
    }

    @Override
    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable) {
        List<Boolean> statuses = opinionStatus == OpinionStatus.NIL ? List.of(true, false) : opinionStatus == OpinionStatus.POSITIVE ? List.of(true) : List.of(false);
        return opinionService.getCommentOpinionsByUser(id, statuses, pageable);
    }
}
