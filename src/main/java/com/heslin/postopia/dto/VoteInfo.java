package com.heslin.postopia.dto;

import com.heslin.postopia.enums.DetailVoteType;

import java.time.Instant;

public record VoteInfo(Long id, Long voteId, DetailVoteType voteType, String initiator, long positiveCount, long negativeCount, Instant startAt, Instant endAt, String additional) {
}