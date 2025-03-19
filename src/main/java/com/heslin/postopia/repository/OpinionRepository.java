package com.heslin.postopia.repository;

import com.heslin.postopia.model.opinion.Opinion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OpinionRepository extends CrudRepository<Opinion, Long> {

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO opinions(opinion_type, updated_at, is_positive, user_id, post_id) " +
                    "VALUES ('POST', :ua, :ip, :uid, :pid) " +
                    "ON CONFLICT (user_id, post_id) WHERE opinion_type = 'POST' " +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive",
            nativeQuery = true
    )
    void upsertPostOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("pid") Long postId
    );

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO opinions(opinion_type, updated_at, is_positive, user_id, vote_id) " +
                    "VALUES ('VOTE', :ua, :ip, :uid, :vid) " +
                    "ON CONFLICT (user_id, vote_id) WHERE opinion_type = 'VOTE' " +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive",
            nativeQuery = true
    )
    void upsertVoteOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("vid") Long voteId
    );

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO opinions(opinion_type, updated_at, is_positive, user_id, comment_id) " +
                    "VALUES ('COMMENT', :ua, :ip, :uid, :cid) " +
                    "ON CONFLICT (user_id, comment_id) WHERE opinion_type = 'COMMENT' " +
                    "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive",
            nativeQuery = true
    )
    void upsertCommentOpinion(
            @Param("ua") Instant updatedAt,
            @Param("ip") Boolean isPositive,
            @Param("uid") Long userId,
            @Param("cid") Long commentId
    );

}
