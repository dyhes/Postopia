package com.heslin.postopia.opinion.dto;

import com.heslin.postopia.opinion.enums.OpinionStatus;

import java.time.Instant;

public record OpinionPart(Long mergeId, OpinionStatus opinionStatus, Instant updatedAt) {
}
