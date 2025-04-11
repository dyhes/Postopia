package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.post.PostDraftDto;
import com.heslin.postopia.jpa.model.PostDraft;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PostDraftRepository extends JpaRepository<PostDraft, Long> {
    @Query("""
        select new com.heslin.postopia.dto.post.PostDraftDto(d.id, d.subject, d.content, s.id, s.name, s.avatar, d.updatedAt) from PostDraft d
            JOIN d.space s
        where d.user.id = :uid
    """)
    Page<PostDraftDto> findPostDraftsByUserId(@Param("uid") Long id, Pageable pageable);

    @Transactional
    @Modifying
    @Query("delete from PostDraft d where d.id = :id and d.user.id = :uid")
    int deletePostDraftById(@Param("id") Long id, @Param("uid") Long uid);

    @Transactional
    @Modifying
    @Query("""
        update PostDraft d set d.subject = :subject, d.content = :content, d.updatedAt = :now
        where d.id = :id and d.user.id = :uid
    """)
    int updatePostDraft(@Param("id") Long id, @Param("subject") String subject,@Param("content") String content, @Param("uid") Long userId, @Param("now") Instant now);

//    @Modifying
//    @Query(
//    value = "INSERT INTO post_drafts (subject, content, user_id, space_id, updated_at) " +
//    "SELECT :subject, :content, :userId, :spaceId, :now " +
//    "WHERE EXISTS (SELECT 1 FROM space_user_info WHERE space_id = :spaceId AND user_id = :userId)",
//    nativeQuery = true
//    )
//    @Transactional
//    int createPostDraft(
//    @Param("subject") String subject,
//    @Param("content") String content,
//    @Param("userId") Long userId,
//    @Param("spaceId") Long spaceId,
//    @Param("now") Instant now
//    );
}
