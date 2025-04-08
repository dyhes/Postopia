package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.PostDraftDto;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.post.SpacePostSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface PostService {
    Pair<Long, Message> createPost(Space space, User user, String subject, String content);
    void authorize(User user, Long postId);
    void deletePost(Long id);
    void archivePost(Long id);
    void unarchivedPost(Long id);
    void updatePost(Long id, String subject, String content);

    Comment replyPost(Post post, String content, User user, Space space);
    void checkPostStatus(Long id);
    void likePost(Long id, @AuthenticationPrincipal User user);
    void disLikePost(Long id, @AuthenticationPrincipal User user);

    PostInfo getPostInfo(Long id, User user);

    Page<SpacePostSummary> getPosts(Long id, Pageable pageable, @AuthenticationPrincipal User user);

    Page<PostSummary> getPostsByUser(boolean isSelf, Long queryId, Long selfId, Pageable pageable);

    Page<UserOpinionPostSummary> getPostOpinionsByUser(Long queryId, OpinionStatus opinion, Pageable pageable);

    boolean draftPost(Space space, User user, String subject, String content, Long id);

    Page<PostDraftDto> getPostDrafts(User user, Pageable pageable);

    boolean deleteDraft(Long id, Long userId);
}
