package com.heslin.postopia.comment.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.vote.dto.VoteInfo;

public record CommentInfo(CommentPart comment, OpinionInfo opinion, UserInfo userInfo, VoteInfo vote){}
