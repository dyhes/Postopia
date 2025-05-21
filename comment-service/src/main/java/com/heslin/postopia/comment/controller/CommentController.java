package com.heslin.postopia.comment.controller;

import com.heslin.postopia.comment.dto.CommentInfo;
import com.heslin.postopia.comment.dto.OpinionCommentInfo;
import com.heslin.postopia.comment.dto.SearchCommentInfo;
import com.heslin.postopia.comment.dto.UserCommentInfo;
import com.heslin.postopia.comment.request.CreateCommentRequest;
import com.heslin.postopia.comment.service.CommentService;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.PageResult;
import com.heslin.postopia.common.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("comment")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createComment(@RequestHeader Long xUserId, @RequestParam CreateCommentRequest request) {
        Long id = commentService.createComment(xUserId, request);
        return ApiResponseEntity.success(id);
    }

    @PostMapping("delete")
    public void deleteComment(@RequestParam Long spaceId, @RequestParam Long postId, @RequestParam Long commentId, @RequestParam Long userId) {
        commentService.deleteComment(spaceId, postId, commentId, userId);
    }

    @GetMapping("pin")
    public boolean checkPinStatus(@RequestParam Long commentId, @RequestParam boolean isPined) {
        return commentService.checkPinStatus(commentId, isPined);
    }

    @PostMapping("pin")
    public void updatePinStatus(@RequestParam Long commentId, @RequestParam boolean isPined) {
        commentService.updatePinStatus(commentId, isPined);
    }

    @GetMapping("post")
    public CompletableFuture<PagedApiResponseEntity<CommentInfo>> getComments(
        @RequestHeader Long xUserId,
        @RequestParam(name = "postId") Long postId,
        @RequestParam int page,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return ApiResponseEntity.ok(new PageResult<>(commentService.getCommentsByPost(postId,user.getId(), pageable)), "success", true);
    }

    @GetMapping("search")
    public CompletableFuture<ApiResponseEntity<List<SearchCommentInfo>>> getCommentInfosInSearch(@RequestParam Long xUserId, @RequestParam List<Long> ids) {
        return commentService.getSearchComments(xUserId, ids).thenApply(ApiResponseEntity::success);
    }

    @GetMapping("user")
    public CompletableFuture<PagedApiResponseEntity<UserCommentInfo>> getComments(
        @RequestHeader Long xUserId,
        @RequestParam int page,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Long queryId = userId == null ? xUserId : userId;
        return commentService.getUserComments(xUserId, queryId, pageable)
        .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("user/opinion")
    public PagedApiResponseEntity<OpinionCommentInfo> getCommentOpinions(
            @RequestHeader Long xUserId,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return commentService.getUserOpinionedComments(queryId, opinion, page, size, direction)
        .thenApply(PagedApiResponseEntity::success);
    }
}
