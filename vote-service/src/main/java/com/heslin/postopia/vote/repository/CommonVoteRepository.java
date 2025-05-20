package com.heslin.postopia.vote.repository;

import com.heslin.postopia.vote.dto.VotePart;
import com.heslin.postopia.vote.enums.VoteType;
import com.heslin.postopia.vote.model.CommonVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommonVoteRepository extends JpaRepository<CommonVote, Long> {

    @Query("""
        select new com.heslin.postopia.vote.dto.VotePart(
            v.id,
            v.initiator,
            v.relatedEntity,
            v.detailVoteType,
            v.positiveCount,
            v.negativeCount,
            v.startAt,
            v.endAt
        ) from CommonVote v where v.voteType = :voteType and v.relatedEntity in :ids
""")
    List<VotePart> findVotes(@Param("ids") List<Long> ids, @Param("voteType") VoteType voteType);

    @Query("""
        select new com.heslin.postopia.dto.VoteInfo(
            v.relatedId,
            v.id,
            v.detailVoteType,
            v.initiator,
            v.positiveCount,
            v.negativeCount,
            v.startAt,
            v.endAt,
            v.additional
        ) from Vote v where v.voteType = com.heslin.postopia.enums.VoteType.SPACE and v.relatedId = :id
""")
    VotePart findBySpace(@Param("id") Long id);

    VotePart findSpaceVote(Long id);
}
