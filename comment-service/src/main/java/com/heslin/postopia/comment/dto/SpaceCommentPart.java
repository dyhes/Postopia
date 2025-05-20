package com.heslin.postopia.comment.dto;

public record SpaceCommentPart(Long id, Long postId, Long spaceId, Long parentId, Long userId, String content, Long positiveCount, Long negativeCount){}