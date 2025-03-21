package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Data;

@Data
public class SpacePostSummary {
    private final Long postId;
    private final String subject;
    private final long positiveCount;
    private final long negativeCount;
    private final long commentCount;
    private final UserId userId;
    private final String nickname;
    private final String avatar;
    private final OpinionStatus opinionStatus;
}
