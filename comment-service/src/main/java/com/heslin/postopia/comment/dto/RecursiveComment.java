package com.heslin.postopia.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class RecursiveComment {
    private final CommentInfo comment;
    private final List<RecursiveComment> children = new ArrayList<>();
}