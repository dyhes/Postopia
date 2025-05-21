package com.heslin.postopia.post.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;

public record UserPostInfo(FeedPostPart post, OpinionInfo opinion) {}
