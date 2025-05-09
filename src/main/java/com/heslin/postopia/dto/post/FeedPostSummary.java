package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class FeedPostSummary extends PostSummary{
    private final String username;
    private final String nickname;
    private final String userAvatar;

    public FeedPostSummary(String spaceName, Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus, String username, String nickname, String userAvatar, Instant createdAt, boolean isArchived) {
        super(spaceName, postId, subject, subContent, positiveCount, negativeCount, commentCount, opinionStatus, createdAt, isArchived);
        this.username = username;
        this.userAvatar = userAvatar;
        this.nickname = nickname;
    }
}
