package com.heslin.postopia.post.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;

public record UserPostInfo(PostPart post, OpinionInfo opinion, UserInfo user){}
