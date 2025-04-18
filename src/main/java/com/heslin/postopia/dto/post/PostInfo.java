package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;

public record PostInfo(String subject, String content, long positiveCount, long negativeCount, long commentCount,
                       String username, String nickname, String avatar, OpinionStatus opinionStatus, boolean isArchived) {

}
