package com.heslin.postopia.comment.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;

public record UserCommentInfo(SpaceCommentPart comment, OpinionInfo opinion) {}
