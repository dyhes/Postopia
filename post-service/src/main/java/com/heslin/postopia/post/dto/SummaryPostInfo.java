package com.heslin.postopia.post.dto;

import java.time.Instant;

public record SummaryPostInfo(Long id, String spaceName, String subject, String content, Long userId, Long positiveCount, Long negativeCount, Instant createdAt) {}