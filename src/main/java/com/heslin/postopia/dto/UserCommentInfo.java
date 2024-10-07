package com.heslin.postopia.dto;

import com.heslin.postopia.model.User;

import java.time.Instant;

public record UserCommentInfo(
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
    public UserCommentInfo {
        userId = User.maskId(userId);
    }
}
