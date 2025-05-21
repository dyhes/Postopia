package com.heslin.postopia.comment.dto;

public record DeleteCommentDetail(Long id, Long postId, Long spaceId, Long userId, String content) {
}
