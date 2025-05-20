package com.heslin.postopia.comment.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;

public record UserCommentInfo(SpaceCommentPart comment, OpinionInfo opinion, UserInfo user) {}
