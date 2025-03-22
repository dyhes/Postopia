package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserOpinionCommentSummary extends UserCommentSummary{
    private final UserId userId;
    private final String nickName;
    private final Instant updatedAt;

    public UserOpinionCommentSummary(Long id, Long spaceId, String spaceName, Long postId, String postSubject, String subContent, Long replyTo, String relpyNickName, Instant createdAt, long positiveCount, long negativeCount, OpinionStatus opinionStatus, Long userId, String nickName, Instant updatedAt) {
        super(id, spaceId, spaceName, postId, postSubject, subContent, replyTo, relpyNickName, createdAt, positiveCount, negativeCount, opinionStatus);
        this.userId = new UserId(userId);
        this.nickName = nickName;
        this.updatedAt = updatedAt;
    }
}
