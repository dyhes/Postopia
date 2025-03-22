package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Data;

public record SpacePostSummary(Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount,
                               UserId userId, String nickname, String avatar, OpinionStatus opinionStatus) {
}
