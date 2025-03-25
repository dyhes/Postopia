package com.heslin.postopia.repository;

import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.model.opinion.Opinion;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OpinionRepository extends CrudRepository<Opinion, Long> {

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO post_opinions(updated_at, is_positive, user_id, post_id) " +
                    "VALUES (:ua, :ip, :uid, :pid) " +
                    "ON CONFLICT (user_id, post_id)" +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
            "RETURNING xmax = 0 AS is_insert",
            nativeQuery = true
    )
    boolean upsertPostOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("pid") Long postId
    );

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO vote_opinions(updated_at, is_positive, user_id, vote_id) " +
                    "VALUES (:ua, :ip, :uid, :vid) " +
                    "ON CONFLICT (user_id, vote_id) " +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
            "RETURNING xmax = 0 AS is_insert",
            nativeQuery = true
    )
    boolean upsertVoteOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("vid") Long voteId
    );

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO comment_opinions(updated_at, is_positive, user_id, comment_id) " +
                    "VALUES (:ua, :ip, :uid, :cid) " +
                    "ON CONFLICT (user_id, comment_id) " +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
                    "RETURNING xmax = 0 AS is_insert",
            nativeQuery = true
    )
    boolean upsertCommentOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("cid") Long commentId
    );

    @Query(
    """
    select new com.heslin.postopia.dto.comment.UserOpinionCommentSummary(
        c.id, s.id, s.name, p.id, p.subject,SUBSTRING(c.content, 1, 100),
                pu.id,
                pu.nickname,
                c.createdAt,
                c.positiveCount,
                c.negativeCount,
                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
                u.id,
                u.nickname,
                o.updatedAt
                ) from CommentOpinion o
                JOIN o.comment c
                JOIN c.post p
                JOIN c.user u
                JOIN p.space s
                LEFT JOIN c.parent.user pu
                where o.user.id = :uid and o.isPositive in :statuses
    """
    )
    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(@Param("uid")Long id, @Param("statuses")List<Boolean> statuses, Pageable pageable);


    @Query(
    """
    select new com.heslin.postopia.dto.post.UserOpinionPostSummary(
        s.id, s.name, p.id, p.subject, SUBSTRING(p.content, 1, 100),
                p.positiveCount,
                p.negativeCount,
                p.commentCount,
                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
                u.id,
                u.nickname,
                u.avatar,    
                o.updatedAt
                ) from PostOpinion o
                JOIN o.post p
                JOIN p.user u
                JOIN p.space s
                where o.user.id = :uid and o.isPositive in :statuses
    """
    )
    Page<UserOpinionPostSummary> getPostOpinionsByUser(@Param("uid")Long id, @Param("statuses")List<Boolean> statuses, Pageable pageable);
}
