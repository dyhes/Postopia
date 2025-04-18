package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserOpinionPostSummary extends UserPostSummary{
    private final String username;
    private final String nickname;
    private final String userAvatar;
    private final Instant updatedAt;

    public UserOpinionPostSummary(String spaceName, Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus, String username, String nickname, String userAvatar, Instant updatedAt, Instant createdAt, boolean isArchived) {
        super(spaceName, postId, subject, subContent, positiveCount, negativeCount, commentCount, opinionStatus, createdAt, isArchived);
        this.username = username;
        this.userAvatar = userAvatar;
        this.nickname = nickname;
        this.updatedAt = updatedAt;
    }
}
