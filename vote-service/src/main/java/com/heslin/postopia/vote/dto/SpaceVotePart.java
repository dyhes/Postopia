package com.heslin.postopia.vote.dto;

import com.heslin.postopia.vote.enums.DetailVoteType;

import java.time.Instant;

public record SpaceVotePart(Long id, Long initiator, Long relatedEntity, Long relatedUser, DetailVoteType voteType, String field1, String field2, long positiveCount, long negativeCount, Instant startAt, Instant endAt) {
}