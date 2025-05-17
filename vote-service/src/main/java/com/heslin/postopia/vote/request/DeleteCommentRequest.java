package com.heslin.postopia.vote.request;

import com.heslin.postopia.common.dto.UserId;

public record DeleteCommentRequest(
Long commentId,
String commentContent,
String commentAuthor,
Long postId,
UserId userId,
String spaceName
){}