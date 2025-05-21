package com.heslin.postopia.opinion.repository;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.dto.VoteOpinionInfo;
import com.heslin.postopia.opinion.model.Opinion;
import com.heslin.postopia.opinion.model.VoteOpinion;
import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Long> {
//    @Query(
//    """
//    select new com.heslin.postopia.dto.comment.UserOpinionCommentSummary(
//        c.id, s.name, p.id, p.subject, SUBSTRING(c.content, 1, 100),
//                pu.username,
//                pu.nickname,
//                c.createdAt,
//                c.positiveCount,
//                c.negativeCount,
//                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
//                u.username,
//                u.nickname,
//                u.avatar,
//                o.updatedAt
//                ) from CommentOpinion o
//                JOIN o.comment c
//                JOIN c.post p
//                JOIN c.user u
//                JOIN p.space s
//                LEFT JOIN c.parent.user pu
//                where o.user.id = :uid and o.isPositive in :statuses
//    """
//    )
//    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(@Param("uid")Long id, @Param("statuses")List<Boolean> statuses, Pageable pageable);
//
//
//    @Query(
//    """
//    select new com.heslin.postopia.dto.post.OpinionFeedPostSummary(
//        s.name, p.id, p.subject, SUBSTRING(p.content, 1, 100),
//                p.positiveCount,
//                p.negativeCount,
//                p.commentCount,
//                CASE WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE END,
//                u.username,
//                u.nickname,
//                u.avatar,
//                p.createdAt,
//                p.isArchived,
//                o.updatedAt
//                ) from PostOpinion o
//                JOIN o.post p
//                JOIN p.user u
//                JOIN p.space s
//                where o.user.id = :uid and o.isPositive in :statuses
//    """
//    )
//    Page<FeedPostSummary> getPostOpinionsByUser(@Param("uid")Long id, @Param("statuses")List<Boolean> statuses, Pageable pageable);
//

    List<VoteOpinion> findVoteOpinionsByVoteId(Long voteId);

    @Query("select new com.heslin.postopia.opinion.dto.VoteOpinionInfo(vo.userId, vo.isPositive) from VoteOpinion vo where vo.voteId = ?1")
    @QueryHints(value = @QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "1000"))
    Stream<VoteOpinionInfo> findStreamVoteOpinions(Long voteId);

    @Modifying
    @Transactional
    @Query("delete PostOpinion po where po.postId = :pid and po.userId = :uid and po.isPositive = :ip")
    int deletePostPinion(@Param("pid") Long postId, @Param("uid") Long userId, @Param("ip") boolean isPositive);

    @Modifying
    @Transactional
    @Query("delete CommentOpinion co where co.commentId = :cid and co.userId= :uid and co.isPositive = :ip")
    int deleteCommentPinion(@Param("cid") Long commentId, @Param("uid") Long userId, @Param("ip") boolean isPositive);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.postId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from PostOpinion o where o.postId in ?2 and o.userId = ?1")
    List<OpinionInfo> getPostOpinion(Long userId, List<Long> idList);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.commentId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from CommentOpinion o where o.commentId in ?2 and o.userId = ?1")
    List<OpinionInfo> getCommentOpinion(Long userId, List<Long> idList);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.voteId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from VoteOpinion o where o.voteId in ?2 and o.userId = ?1")
    List<OpinionInfo> getVoteOpinion(Long userId, List<Long> idList);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.postId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from PostOpinion o where o.userId = ?1")
    Page<OpinionInfo> findPostOpinionsByUserId(Long userId, Pageable pageable);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.postId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from PostOpinion o where o.userId = ?1 and o.isPositive = ?2")
    Page<OpinionInfo> findPostOpinionByUserIdAndPositive(Long userId, boolean positive, Pageable pageable);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.commentId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from CommentOpinion o where o.userId = ?1")
    Page<OpinionInfo> findCommentOpinionsByUserId(Long userId, Pageable pageable);

    @Query("select new com.heslin.postopia.opinion.dto.OpinionInfo(o.commentId, CASE WHEN o.isPositive = true THEN com.heslin.postopia.opinion.enums.OpinionStatus.POSITIVE ELSE com.heslin.postopia.opinion.enums.OpinionStatus.NEGATIVE END, o.updatedAt) from CommentOpinion o where o.userId = ?1 and o.isPositive = ?2")
    Page<OpinionInfo> findCommentOpinionByUserIdAndPositive(Long userId, boolean positive, Pageable pageable);

    @Transactional
    @Modifying
    @Query("delete from PostOpinion po where po.postId in ?1")
    void deletePostPinionInBatch(List<Long> list);

    @Transactional
    @Modifying
    @Query("delete from CommentOpinion co where co.commentId in ?1")
    void deleteCommentPinionInBatch(List<Long> list);
}
