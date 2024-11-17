package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface  CommentService {
    Comment replyToPost(Post post, String content, @AuthenticationPrincipal User user);

    void reply(Post post, Comment comment, String content, @AuthenticationPrincipal User user);

    void deleteComment(Long id);

    void checkAuthority(Long id, @AuthenticationPrincipal User user);

    void likeComment(Long id);

    void disLikeComment(Long id);

    Page<CommentSummary> getCommentsByUser(Long id, Pageable pageable);

    List<CommentInfo> getCommentsByPost(Long postId);
}
