package com.heslin.postopia.vote.dto;

import com.heslin.postopia.vote.enums.DetailVoteType;

import java.time.Instant;

public record VoteInfo(Long id, Long voteId, DetailVoteType voteType, String initiator, long positiveCount, long negativeCount, Instant startAt, Instant endAt, String additional) {
}