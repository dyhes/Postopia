package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface  CommentService {
    Comment replyToPost(Post post, String content, @AuthenticationPrincipal User user);

    void reply(Post post, Comment comment, String content, @AuthenticationPrincipal User user);

    void deleteComment(Long id);

    void checkAuthority(Long id, @AuthenticationPrincipal User user);

    void likeComment(Long id, @AuthenticationPrincipal User user);

    void disLikeComment(Long id, @AuthenticationPrincipal User user);

    Page<CommentSummary> getCommentsByUser(Long queryId, Long selfId, Pageable pageable);

    Page<CommentInfo> getCommentsByPost(Long postId, Long userId, Pageable pageable);

    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable);
}
