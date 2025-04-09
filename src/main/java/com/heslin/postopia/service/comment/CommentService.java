package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface  CommentService {
    Comment replyToPost(Post post, String content, User user, Space space);

    Comment reply(Post post, Comment comment, String content, User user, Space space);

    void deleteComment(Long id, Long postId, Long userId);

    void checkAuthority(Long id, @AuthenticationPrincipal User user);

    void likeComment(Long id, @AuthenticationPrincipal User user);

    void disLikeComment(Long id, @AuthenticationPrincipal User user);

    Page<CommentSummary> getCommentsByUser(Long queryId, Long selfId, Pageable pageable);

    Page<CommentInfo> getCommentsByPost(Long postId, Long userId, Pageable pageable);

    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable);

    List<SearchedCommentInfo> getCommentInfosInSearch(List<Long> ids);
}
