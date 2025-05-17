package com.heslin.postopia.message.dto;

import java.time.Instant;

public record MessageInfo(Long id, String content, boolean isRead, Instant createdAt) {
}