package com.heslin.postopia.service.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.util.Pair;

public interface PostService {
    Pair<Long, Message> createPost(boolean isDraft, Space space, User user, String subject, String content);
    void authorize(User user, Long postId);
    void deletePost(Long id);
    void archivePost(Long id);
    void unarchivePost(Long id);
    void updatePost(Long id, String subject, String content);
    void replyPost(Long id, String content);
    void checkPostStatus(Long id);
    PostInfo getPostInfo(Long id);
    Page<PostSummary> getPosts(Long id, Pageable pageable);
}
