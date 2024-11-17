package com.heslin.postopia.dto.comment;

import com.heslin.postopia.model.User;

import java.time.Instant;

public record CommentSummary(
        Long id,
        String content,
        Instant createdAt,
        Long spaceId,
        Long postId,
        String subject,
        Long userId,
        String nickName,
        String avatar) {

    // Compact constructor for validation/modification
    public CommentSummary {
        userId = User.maskId(userId);
    }
}
