package com.heslin.postopia.elasticsearch.dto;

import java.time.Instant;

public record SearchedCommentInfo(Long id, String nickname, String userAvatar, long positiveCount, long negativeCount, Instant createdAt) {
}
