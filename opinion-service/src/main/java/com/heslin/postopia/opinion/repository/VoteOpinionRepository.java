package com.heslin.postopia.opinion.repository;

import com.heslin.postopia.opinion.dto.VoteOpinionInfo;
import com.heslin.postopia.opinion.model.VoteOpinion;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface VoteOpinionRepository extends JpaRepository<VoteOpinion, Long> {


    @Query("select new com.heslin.postopia.opinion.dto.VoteOpinionInfo(vo.userId, vo.isPositive) from VoteOpinion vo where vo.voteId = ?1")
    @QueryHints(value = @QueryHint(name = AvailableHints.HINT_FETCH_SIZE, value = "1000"))
    Stream<VoteOpinionInfo> findVoteOpinionsByVoteId(Long voteId);
}
