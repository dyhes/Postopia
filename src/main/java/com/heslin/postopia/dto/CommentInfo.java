package com.heslin.postopia.dto;

import com.heslin.postopia.model.User;

import java.time.Instant;

public record CommentInfo(
        Long id,
        String content,
        Instant time,
        Long spaceId,
        Long postId,
        String subject,
        Long userId,
        String nickName,
        String avatar) {

    // Compact constructor for validation/modification
    public CommentInfo {
        userId = User.maskId(userId);
    }
}
