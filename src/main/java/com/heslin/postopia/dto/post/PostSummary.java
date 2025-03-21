package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.UserId;
import com.heslin.postopia.enums.OpinionStatus;

public record PostSummary(Long postId, String subject, long positiveCount, long negativeCount, long commentCount, UserId userId,
                          String nickname, String avatar, OpinionStatus opinionStatus) {

}
