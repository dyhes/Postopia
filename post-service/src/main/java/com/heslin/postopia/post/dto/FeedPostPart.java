package com.heslin.postopia.post.dto;

import java.time.Instant;

public record FeedPostPart(Long id, Long userId, Long spaceId, String spaceName, String subject, String content, long positiveCount, long negativeCount, long commentCount, boolean isArchived, Instant createdAt) {
}
