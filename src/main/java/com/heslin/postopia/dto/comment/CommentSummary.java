package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;


// 用户自己视角下的自己发表的评论概要
@Getter
@AllArgsConstructor
public class CommentSummary {
    private final Long id;
    private final String spaceName;
    private final Long postId;
    private final String postSubject;
    private final String subContent;
    private final String replyUsername;
    private final String replyNickname;
    private final Instant createdAt;
    private final long positiveCount;
    private final long negativeCount;
    private final OpinionStatus opinionStatus;
}
