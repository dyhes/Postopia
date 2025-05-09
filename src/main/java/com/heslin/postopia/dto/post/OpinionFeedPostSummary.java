package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class OpinionFeedPostSummary extends FeedPostSummary {
    private final Instant updatedAt;

    public OpinionFeedPostSummary(String spaceName, Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus, String username, String nickname, String userAvatar, Instant createdAt, boolean isArchived, Instant updatedAt) {
        super(spaceName, postId, subject, subContent, positiveCount, negativeCount, commentCount, opinionStatus, username, nickname, userAvatar, createdAt, isArchived);
        this.updatedAt = updatedAt;
    }
}
