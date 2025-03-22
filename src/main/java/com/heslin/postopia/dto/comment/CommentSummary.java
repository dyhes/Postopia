package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import lombok.Getter;

import java.time.Instant;


// 用户自己视角下的自己发表的评论概要
@Getter
public class CommentSummary {
    private final Long id;
    private final Long spaceId;
    private final String spaceName;
    private final Long postId;
    private final String postSubject;
    private final String subContent;
    private final UserId replyTo;
    private final String replyNickName;
    private final Instant createdAt;
    private final long positiveCount;
    private final long negativeCount;

    public CommentSummary(Long id, Long spaceId, String spaceName, Long postId, String postSubject, String subContent, Long replyTo, String nickName, Instant createdAt, long positiveCount, long negativeCount) {
        this.id = id;
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.postId = postId;
        this.postSubject = postSubject;
        this.subContent = subContent;
        this.replyTo = new UserId(replyTo);
        this.replyNickName = nickName;
        this.createdAt = createdAt;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }
}
