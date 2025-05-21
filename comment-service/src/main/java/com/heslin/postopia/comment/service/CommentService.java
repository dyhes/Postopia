package com.heslin.postopia.comment.service;

import com.heslin.postopia.comment.dto.CommentOpinionHint;
import com.heslin.postopia.comment.model.Comment;
import com.heslin.postopia.comment.repository.CommentRepository;
import com.heslin.postopia.comment.request.CreateCommentRequest;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.PostOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.search.model.CommentDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Math.min;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final KafkaService kafkaService;

    @Autowired
    public CommentService(CommentRepository commentRepository, KafkaService kafkaService) {
        this.commentRepository = commentRepository;
        this.kafkaService = kafkaService;
    }

    public List<CommentOpinionHint> getOpinionHints(List<Long> list) {
        return commentRepository.findOpinionHints(list);
    }

    public Long createComment(Long xUserId, String xUsername, CreateCommentRequest request) {
        Comment parent  = request.parentId() != null? Comment.builder().id(request.parentId()).build() : null;
        Comment comment = Comment.builder()
        .spaceId(request.spaceId())
        .postId(request.postId())
        .userId(xUserId)
        .parent(parent)
        .isPined(false)
        .content(request.content())
        .build();
        comment = commentRepository.save(comment);

        String parentId = request.parentId() != null? request.parentId().toString() : null;

        kafkaService.sendToDocCreate("comment", comment.getId().toString(),
        new CommentDoc(
            comment.getId(),
            comment.getContent(),
            request.spaceId().toString(),
            request.postId().toString(),
            parentId,
            xUserId.toString()));
        kafkaService.sendToPost(request.postId(), PostOperation.COMMENT_CREATED);
        kafkaService.sendToUser(xUserId, UserOperation.COMMENT_CREATED);
        kafkaService.sendToUser(request.userId(), UserOperation.CREDIT_EARNED);

        StringBuilder messageContent = new StringBuilder();
        messageContent
        .append(PostopiaFormatter.formatUser(xUserId, xUsername))
        .append("回复了您：")
        .append(request.content(), 0, min(request.content().length(), 20))
        .append("... %s".formatted(PostopiaFormatter.formatComment(request.spaceId(), request.postId(), comment.getId())));
        kafkaService.sendMessage(request.userId(), messageContent.toString());

        return comment.getId();
    }

    public void deleteComment(Long spaceId, Long postId, Long commentId, Long userId) {
        // not impl
        // query children comments
//        boolean success = commentRepository.deleteById(commentId); > 0;
//        if (success) {
//            kafkaService.sendToPost(postId, PostOperation.COMMENT_DELETED);
//            kafkaService.sendToUser(userId, UserOperation.COMMENT_DELETED);
//            kafkaService.sendToDocDelete("comment", commentId.toString(), spaceId.toString());
//        }
    }

    public boolean checkPinStatus(Long commentId, boolean isPined) {
        return commentRepository.checkCommentPinStatus(commentId, isPined) == 0;
    }

    public void updatePinStatus(Long commentId, boolean isPined) {
        commentRepository.updateCommentPinStatus(commentId, isPined);
    }

//    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
//        return opinionRepository.getCommentOpinionsByUser(id, statuses, pageable);
//    }
}
