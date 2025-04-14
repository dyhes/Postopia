package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.comment.CommentVote;
import com.heslin.postopia.jpa.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("""
        select new com.heslin.postopia.dto.comment.CommentVote(
            v.relatedId,
            v.id,
            v.detailVoteType,
            v.initiator,
            v.positiveCount,
            v.negativeCount,
            v.startAt,
            v.endAt
        ) from Vote v where v.voteType = com.heslin.postopia.enums.VoteType.COMMENT and v.relatedId in :relatedIds
""")
    List<CommentVote> findCommentVotes(@Param("ids") List<Long> commentIds);
}
