package com.heslin.postopia.dto;

import java.time.Instant;

public record UserMessage(Long id, String content, boolean isRead, Instant createdAt) {
}
