package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.jpa.model.opinion.Opinion;
import com.heslin.postopia.jpa.model.opinion.VoteOpinion;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Long> {
    @Query(
    """
    select new com.heslin.postopia.dto.comment.UserOpinionCommentSummary(
        c.id, s.name, p.id, p.subject, SUBSTRING(c.content, 1, 100),
                pu.username,
                pu.nickname,
                c.createdAt,
                c.positiveCount,
                c.negativeCount,
                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
                u.username,
                u.nickname,
                u.avatar,
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
        s.name, p.id, p.subject, SUBSTRING(p.content, 1, 100),
                p.positiveCount,
                p.negativeCount,
                p.commentCount,
                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
                u.username,
                u.nickname,
                u.avatar,
                o.updatedAt,
                p.createdAt
                ) from PostOpinion o
                JOIN o.post p
                JOIN p.user u
                JOIN p.space s
                where o.user.id = :uid and o.isPositive in :statuses
    """
    )
    Page<UserOpinionPostSummary> getPostOpinionsByUser(@Param("uid")Long id, @Param("statuses")List<Boolean> statuses, Pageable pageable);


    @Modifying
    @Transactional
    @Query("delete PostOpinion po where po.post.id = :pid and po.user.id = :uid and po.isPositive = :ip")
    int deletePostPinion(@Param("pid") Long postId, @Param("uid") Long userId, @Param("ip") boolean isPositive);

    @Modifying
    @Transactional
    @Query("delete CommentOpinion co where co.comment.id = :cid and co.user.id = :uid and co.isPositive = :ip")
    int deleteCommentPinion(@Param("cid") Long commentId, @Param("uid") Long userId, @Param("ip") boolean isPositive);

    List<VoteOpinion> findVoteOpinionsByVoteId(Long voteId);
}
