package com.heslin.postopia.service.comment;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface  CommentService {
    Comment replyToPost(Post post, String content, User user, Space space, Long replyUserId, String replyUser);

    void reply(Post post, Comment comment, String content, User user, Space space, Long replyUserId, String replyUser);

    void deleteComment(Long id, Long postId, Long userId, String spaceName);

    Page<CommentSummary> getCommentsByUser(Long queryId, Long selfId, Pageable pageable);

    Page<CommentInfo> getCommentsByPost(Long postId, Long userId, Pageable pageable);

    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable);

    List<SearchedCommentInfo> getCommentInfosInSearch(List<Long> ids);

    void upsertCommentOpinion(User user, Long id, Long postId, Long userId, String spaceName, boolean isPositive);

    boolean deleteCommentOpinion(User user, Long id, boolean isPositive);

    List<AuthorHint> getAuthorHints(List<Long> commentIds);

    boolean checkCommentPinStatus(Long commentId, boolean isPined);

    void updatePinStatus(Long commentId, boolean isPined);

    void validate(User user, String spaceName);

    List<String> getCommentContents(Long postId);

    List<Pair<Long, Long>> getDeleteCommentInfosByPost(Long postId);
}
