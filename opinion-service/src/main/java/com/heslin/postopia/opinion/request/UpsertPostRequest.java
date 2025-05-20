package com.heslin.postopia.opinion.request;

public record UpsertPostRequest(Long postId, Long spaceId, Long userId, boolean isPositive) {
}
