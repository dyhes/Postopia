package com.heslin.postopia.vote.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;

public record SpaceVoteInfo(SpaceVotePart vote, UserInfo initiator, OpinionInfo opinion, UserInfo relatedUser) {}
