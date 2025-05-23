package com.heslin.postopia.post.repository;

import com.heslin.postopia.post.dto.*;
import com.heslin.postopia.post.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Transactional
    @Query("delete from Post p where p.id = ?1")
    int deletePost(Long postId);

    List<PostOpinionHint> findPOHByIdIn(Collection<Long> ids);

    @Modifying
    @Transactional
    @Query("update Post p set p.subject = :subject, p.content = :content where p.id = :id and p.userId = :uid and p.isArchived = false ")
    int updateSubjectAndContent(@Param("id")Long id, @Param("uid")Long uid, @Param("subject")String subject, @Param("content")String content);

    @Query("select count(*) from Post p where p.id = :pid and p.isArchived = :isArchived")
    int checkPostArchiveStatus(@Param("pid") Long postId, @Param("isArchived") boolean isArchived);

    @Modifying
    @Transactional
    @Query("update Post p set p.isArchived = :isArchived where p.id = :pid")
    void updateArchiveStatus(@Param("pid") Long postId, @Param("isArchived") boolean isArchived);

    Page<FeedPostPart> findByUserId(Long userId, Pageable pageable);

    Page<PostPart> findBySpaceId(Long spaceId, Pageable pageable);

    PostPart findPostPartById(Long postId);

    List<FeedPostPart> findFeedPostByIdIn(Collection<Long> ids);

    List<CommentPostInfo> findCommentPostInfosByIdIn(Collection<Long> ids);

    SummaryPostInfo findSummaryById(Long id);

    Page<FeedPostPart> findByCommentCountGreaterThanEqual(Long commentCountIsGreaterThan, Pageable pageable);
}
