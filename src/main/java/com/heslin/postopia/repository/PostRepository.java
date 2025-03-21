package com.heslin.postopia.repository;

import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>{
    @Query("select p.user.id from Post p where p.id =:id")
    Optional<Long> findUserIdById(@Param("id") Long id);

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

    @Modifying
    @Transactional
    @Query("update Post p set p.positiveCount = p.positiveCount + 1 where p.id = :id")
    void likePost(@Param("id")Long id);

    @Modifying
    @Transactional
    @Query("update Post p set p.negativeCount = p.negativeCount + 1 where p.id = :id")
    void disLikePost(@Param("id")Long id);

    Optional<PostStatus> findStatusById(Long id);

    @Query("""
             select
                 new com.heslin.postopia.dto.post.PostInfo(p.subject, p.content, p.positiveCount, p.negativeCount, p.commentCount, u.username, u.nickname, u.avatar,
                     CASE
                         WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                         WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                         ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                     END)
            from Post p
            JOIN p.user u
            LEFT JOIN PostOpinion o on o.user.id = :uid and o.post.id = :id
            where p.id = :id
            """)
    Optional<PostInfo> findPostInfoById(@Param("id") Long id, @Param("uid") Long userId);

    @Query("""
            select
            new com.heslin.postopia.dto.post.PostSummary(p.id, p.subject, p.positiveCount, p.negativeCount, p.commentCount, new com.heslin.postopia.dto.UserId(u.id), u.nickname, u.avatar,
                    CASE
                        WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                        WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                        ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                    END)
                    from Post p
                    JOIN p.user u
                    LEFT JOIN PostOpinion o on o.post.id = p.id and o.user.id = :uid
                    where p.space.id = :id and p.status != com.heslin.postopia.enums.PostStatus.DRAFT
            """)
    Page<PostSummary> findPostSummariesBySpaceId(@Param("id") Long id, @Param("uid") Long userId, Pageable pageable);


    @Query("""
            select new com.heslin.postopia.dto.post.PostSummary(p.id, p.subject, p.positiveCount, p.negativeCount, p.commentCount, new com.heslin.postopia.dto.UserId(u.id), u.nickname, u.avatar, null)
            from Post p
            JOIN p.user u
            where u.id = :uid
            """)
    Page<PostSummary> findPostSummariesByUserId(@Param("uid") Long id, Pageable pageable);
}
