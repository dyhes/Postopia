package com.heslin.postopia.elasticsearch.dto;

import java.time.Instant;

public record SearchedPostInfo(Long id, String subject, long positiveCount, long negativeCount, long commentCount, String spaceAvatar, Instant createdAt) {
}
