package com.heslin.postopia.comment.dto;

import java.time.Instant;

public record CommentPart(Long id, Long parentId, Long userId, String content, boolean isPined, Long positiveCount, Long negativeCount, Instant createdAt){}
