package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public interface PostService {
    Pair<Long, Message> createPost(boolean isDraft, Space space, User user, String subject, String content);
    void authorize(User user, Long postId);
    void deletePost(Long id);
    void archivePost(Long id);
    void unarchivedPost(Long id);
    void updatePost(Long id, String subject, String content);

    Comment replyPost(Long id, String content, User user);
    void checkPostStatus(Long id);
    void likePost(Long id, @AuthenticationPrincipal User user);
    void disLikePost(Long id, @AuthenticationPrincipal User user);

    PostInfo getPostInfo(Long id, User user);

    Page<PostSummary> getPosts(Long id, Pageable pageable, @AuthenticationPrincipal User user);

    Page<PostSummary> getPostsByUser(Long id, Pageable pageable);
}
