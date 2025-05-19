package com.heslin.postopia.vote.request;

public record CommentVoteRequest(
Long commentId,
Long postId,
Long spaceId,
Long userId,
String commentContent
){}