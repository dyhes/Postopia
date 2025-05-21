package com.heslin.postopia.comment.request;

public record CreateCommentRequest(Long parentId, Long userId, Long postId, Long spaceId, String content) {
}
