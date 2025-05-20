package com.heslin.postopia.post.dto;

import java.time.Instant;

public record PostPart(Long id, Long userId, String subject, String content, long positiveCount, long negativeCount, long commentCount, boolean isArchived, Instant createdAt){}
