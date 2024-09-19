package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.service.opinion.OpinionService;

public record PostSummary(String subject, long positive, long negative, long comment, String username, String nickname, String avatar, OpinionStatus opinionStatus) {

}
