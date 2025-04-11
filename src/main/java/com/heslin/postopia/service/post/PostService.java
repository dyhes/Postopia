package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.post.PostDraftDto;
import com.heslin.postopia.dto.post.*;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
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

public interface PostService {
    Pair<Long, ResMessage> createPost(Space space, User user, String subject, String content);
    void authorize(User user, Long postId);
    boolean deletePost(Long id, Long userId, String spaceName);
    void archivePost(Long id);
    void unarchivedPost(Long id);
    boolean updatePost(Long id, Long userId, String spaceName, String subject, String content);

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

    List<SearchedPostInfo> getPostInfosInSearch(List<Long> ids);
}
