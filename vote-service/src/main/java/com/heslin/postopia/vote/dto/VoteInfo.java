package com.heslin.postopia.vote.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;

public record VoteInfo(Long mergeId, VotePart vote, UserInfo initiator, OpinionInfo opinion) {
    public VoteInfo(Long mergeId) {
        this(mergeId, null, null, null);
    }
}
