package com.heslin.postopia.vote.repository;

import com.heslin.postopia.vote.dto.SpaceVotePart;
import com.heslin.postopia.vote.model.SpaceVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceVoteRepository extends JpaRepository<SpaceVote, Long> {
    List<SpaceVotePart> findByRelatedEntity(Long relatedEntity);
}
