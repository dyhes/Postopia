package com.heslin.postopia.post.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.vote.dto.VoteInfo;

public record FeedPostInfo(FeedPostPart post, OpinionInfo opinion, UserInfo user, VoteInfo vote) {}
