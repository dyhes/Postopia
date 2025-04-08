package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 用户自身视角的自己的帖子摘要
@Getter
@AllArgsConstructor
public class PostSummary {
    private final String spaceName;
    private final Long postId;
    private final String subject;
    private final String subContent;
    private final long positiveCount;
    private final long negativeCount;
    private final long commentCount;
    private final Instant createdAt;
}
