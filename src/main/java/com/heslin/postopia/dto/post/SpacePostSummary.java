package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Data;

import java.time.Instant;

public record SpacePostSummary(Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount,
                               String username, String nickname, String userAvatar, OpinionStatus opinionStatus, Instant createdAt) {
}
