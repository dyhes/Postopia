package com.heslin.postopia.comment.dto;

import java.time.Instant;

public record SearchCommentPart(Long id, Long postId, Long userId, Long positiveCount, Long negativeCount, Instant createdAt) {
}
