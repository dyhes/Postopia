package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;

import java.time.Instant;

public record CommentSummary(
        Long id,
        String content,
        Instant createdAt,
        Long spaceId,
        Long postId,
        String subject,
        UserId userId,
        String nickName,
        String avatar) {

    // Compact constructor for validation/modification
}
