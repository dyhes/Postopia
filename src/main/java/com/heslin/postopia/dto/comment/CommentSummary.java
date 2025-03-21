package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class CommentSummary {
    private final Long id;
    private final Long spaceId;
    private final Long postId;
    private final String content;
    private final Instant createdAt;
    private final long positiveCount;
    private final long negativeCount;
}
