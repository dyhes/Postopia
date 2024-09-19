package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;

public record PostInfo(String subject, String content, long positive, long negative, long comment, String username, String nickname, String avatar, OpinionStatus opinionStatus) {

}
