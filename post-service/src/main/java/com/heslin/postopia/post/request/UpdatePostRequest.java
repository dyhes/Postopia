package com.heslin.postopia.post.request;

public record UpdatePostRequest(Long postId, Long spaceId, String subject, String content) { }
