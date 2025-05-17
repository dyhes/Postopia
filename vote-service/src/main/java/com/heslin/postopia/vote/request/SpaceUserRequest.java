package com.heslin.postopia.vote.request;

public record SpaceUserRequest(Long spaceId, Long userId, String username, String reason) {
}
