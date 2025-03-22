package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserCommentSummary extends CommentSummary {
    private final OpinionStatus opinionStatus;


    public UserCommentSummary(Long id, Long spaceId, String spaceName, Long postId, String postSubject, String subContent, Long replyTo, String nickName, Instant createdAt, long positiveCount, long negativeCount, OpinionStatus opinionStatus) {
        super(id, spaceId, spaceName, postId, postSubject, subContent, replyTo, nickName, createdAt, positiveCount, negativeCount);
        this.opinionStatus = opinionStatus;
    }
}
