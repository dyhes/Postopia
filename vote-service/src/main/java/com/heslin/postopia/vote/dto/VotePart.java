package com.heslin.postopia.vote.dto;

import com.heslin.postopia.vote.enums.DetailVoteType;

import java.time.Instant;

public record VotePart(Long id, Long initiator, Long relatedEntity, DetailVoteType voteType, long positiveCount, long negativeCount, Instant startAt, Instant endAt) {
}