package com.heslin.postopia.comment.dto;

import java.time.Instant;

public record SummaryCommentInfo(Long id, String content, Long userId, long positiveCount, long negativeCount, Instant createdAt) {}
