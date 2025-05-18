package com.heslin.postopia.vote.request;

public record PostVoteRequest(
    Long postId,
    String postSubject,
    Long spaceId,
    Long userId
){}