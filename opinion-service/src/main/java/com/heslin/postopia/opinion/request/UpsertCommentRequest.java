package com.heslin.postopia.opinion.request;

public record UpsertCommentRequest(Long spaceId, Long commentId, Long userId, boolean isPositive){}
