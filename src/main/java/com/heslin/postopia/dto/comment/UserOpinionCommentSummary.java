package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserOpinionCommentSummary extends UserCommentSummary{
    private final String username;
    private final String nickname;
    private final String userAvatar;
    private final Instant updatedAt;

    public UserOpinionCommentSummary(Long id, String spaceName, Long postId, String postSubject, String subContent, String replyUsername, String relpyNickName, Instant createdAt, long positiveCount, long negativeCount, OpinionStatus opinionStatus, String username, String nickName, String userAvatar, Instant updatedAt) {
        super(id, spaceName, postId, postSubject, subContent, replyUsername, relpyNickName, createdAt, positiveCount, negativeCount, opinionStatus);
        this.username = username;
        this.nickname = nickName;
        this.updatedAt = updatedAt;
        this.userAvatar = userAvatar;
    }
}
