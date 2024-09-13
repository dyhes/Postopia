package com.heslin.postopia.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.model.Post;

import jakarta.transaction.Transactional;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>{
    Optional<Long> findUserIdById(Long id);

    @Modifying
    @Transactional
    @Query("update Post p set p.status = :status where p.id = :id")
    void updateStatus(@Param("id")Long id, @Param("status")PostStatus status);

    @Modifying
    @Transactional
    @Query("update Post p set p.subject = :subject, p.content = :content where p.id = :id")
    void updateSubjectAndContent(@Param("id")Long id, @Param("subject")String subject, @Param("content")String content);

    @Modifying
    @Transactional
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :id")
    void addComment(@Param("id")Long id);

    Optional<PostStatus> findStatusById(Long id);

    @Query("select new com.heslin.postopia.dto.post.PostInfo(p.subject, p.content, p.positiveCount, p.negativeCount, p.commentCount, u.username, u.nickname, u.avatar) from Post p JOIN p.user u where p.id = :id")
    Optional<PostInfo> findPostInfoById(@Param("id")Long id);

    @Query("select new com.heslin.postopia.dto.post.PostSummary(p.subject, p.positiveCount, p.negativeCount, p.commentCount, u.username, u.nickname, u.avatar) from Post p JOIN p.user u where p.space.id = :id and p.status != com.heslin.postopia.enums.PostStatus.DRAFT")
    Page<PostSummary> findPostSummariesBySpaceId(@Param("id") Long id, Pageable pageable);
}
