package com.heslin.postopia.post.request;

public record CreatePostRequest(Long spaceId, String spaceName, String subject, String content) {
}
