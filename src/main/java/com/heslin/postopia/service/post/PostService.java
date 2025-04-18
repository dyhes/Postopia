package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.AuthorHint;
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
    void deletePost(Long id, String spaceName);
    boolean updatePost(Long id, Long userId, String spaceName, String subject, String content);

    Comment replyPost(Post post, String content, User user, Space space, String replyUser);

    void upsertPostOpinion(User user, Long id, String spaceName, boolean isPositive);

    boolean deletePostOpinion(User user, Long id, boolean isPositive);

    PostInfo getPostInfo(Long id, User user);

    Page<SpacePostSummary> getPosts(Long id, Pageable pageable, @AuthenticationPrincipal User user);

    Page<PostSummary> getPostsByUser(boolean isSelf, Long queryId, Long selfId, Pageable pageable);

    Page<UserOpinionPostSummary> getPostOpinionsByUser(Long queryId, OpinionStatus opinion, Pageable pageable);

    boolean draftPost(Space space, User user, String subject, String content, Long id);

    Page<PostDraftDto> getPostDrafts(User user, Pageable pageable);

    boolean deleteDraft(Long id, Long userId);

    List<SearchedPostInfo> getPostInfosInSearch(List<Long> ids);

    List<AuthorHint> getAuthorHints(List<Long> postIds);

    boolean checkPostArchiveStatus(Long postId, boolean isArchived);

    void updateArchiveStatus(Long postId, boolean isArchived);
}
