package com.heslin.postopia.comment.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RecursiveComment {
    private final CommentInfo comment;
    private final List<RecursiveComment> children = new ArrayList<>();

    public RecursiveComment(CommentInfo comment) {
        this.comment = comment;
    }
}