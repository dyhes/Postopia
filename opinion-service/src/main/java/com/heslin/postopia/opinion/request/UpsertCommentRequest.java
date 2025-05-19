package com.heslin.postopia.opinion.request;

public record UpsertCommentRequest(Long spaceId, Long postId, Long commentId, Long userId, boolean isPositive){}
