package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserOpinionPostSummary extends UserPostSummary{
    private final UserId userId;
    private final String nickName;
    private final String avatar;
    private final Instant updatedAt;

    public UserOpinionPostSummary(Long spaceId, String spaceName, Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus, Long userId, String nickName, String avatar, Instant updatedAt) {
        super(spaceId, spaceName, postId, subject, subContent, positiveCount, negativeCount, commentCount, opinionStatus);
        this.userId = new UserId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
        this.updatedAt = updatedAt;
    }
}
