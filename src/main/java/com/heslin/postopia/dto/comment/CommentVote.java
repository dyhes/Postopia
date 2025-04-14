package com.heslin.postopia.dto.comment;

import com.heslin.postopia.enums.DetailVoteType;

import java.time.Instant;

public record CommentVote(Long commentId, Long voteId, DetailVoteType voteType, String initiator, long positiveCount, long negativeCount, Instant startAt, Instant endAt) {
}
