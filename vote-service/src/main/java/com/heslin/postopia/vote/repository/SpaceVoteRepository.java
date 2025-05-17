package com.heslin.postopia.vote.repository;

import com.heslin.postopia.vote.model.SpaceVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceVoteRepository extends JpaRepository<SpaceVote, Long> {
}
