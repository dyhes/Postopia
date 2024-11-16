package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;

public record PostSummary(String subject, long positiveCount, long negativeCount, long commentCount, String username,
                          String nickname, String avatar, OpinionStatus opinionStatus) {

}
