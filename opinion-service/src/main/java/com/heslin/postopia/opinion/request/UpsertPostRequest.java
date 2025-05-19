package com.heslin.postopia.opinion.request;

public record UpsertPostRequest(Long postId, String spaceId, Long userId, boolean isPositive) {
}
