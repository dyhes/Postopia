package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.VoteInfo;
import com.heslin.postopia.enums.VoteType;
import com.heslin.postopia.jpa.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("""
        select new com.heslin.postopia.dto.VoteInfo(
            v.relatedId,
            v.id,
            v.detailVoteType,
            v.initiator,
            v.relatedUser,
            v.positiveCount,
            v.negativeCount,
            v.startAt,
            v.endAt
        ) from Vote v where v.voteType = :voteType and v.relatedId in :ids
""")
    List<VoteInfo> findVotes(@Param("ids") List<Long> ids, @Param("voteType") VoteType voteType);
}
