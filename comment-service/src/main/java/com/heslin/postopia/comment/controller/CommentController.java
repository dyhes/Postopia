package com.heslin.postopia.comment.controller;

import com.heslin.postopia.comment.dto.OpinionCommentInfo;
import com.heslin.postopia.comment.dto.RecursiveComment;
import com.heslin.postopia.comment.dto.SearchCommentInfo;
import com.heslin.postopia.comment.dto.UserCommentInfo;
import com.heslin.postopia.comment.request.CreateCommentRequest;
import com.heslin.postopia.comment.service.CommentService;
import com.heslin.postopia.comment.service.IntelligenceService;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("comment")
public class CommentController {
    private final CommentService commentService;
    private final IntelligenceService intelligenceService;

    @Autowired
    public CommentController(CommentService commentService, IntelligenceService intelligenceService) {
        this.commentService = commentService;
        this.intelligenceService = intelligenceService;
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createComment(@RequestHeader Long xUserId, @RequestHeader String xUsername, @RequestBody CreateCommentRequest request) {
        Long id = commentService.createComment(xUserId, xUsername, request);
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
    public CompletableFuture<PagedApiResponseEntity<RecursiveComment>> getComments(
        @RequestHeader Long xUserId,
        @RequestParam(name = "postId") Long postId,
        @RequestParam int page,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(Sort.Direction.DESC, "isPined"), new Sort.Order(direction, "createdAt")));
        return commentService.getCommentsByPost(xUserId, postId, pageable).thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("info")
    public CompletableFuture<ApiResponseEntity<RecursiveComment>> getComment(
    @RequestHeader Long xUserId,
    @RequestParam Long commentId
    ) {
        return commentService.getComment(xUserId, commentId).thenApply(ApiResponseEntity::success);
    }

    @GetMapping("search")
    public CompletableFuture<ApiResponseEntity<List<SearchCommentInfo>>> getCommentInfosInSearch(@RequestHeader Long xUserId, @RequestParam List<Long> ids) {
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
    public CompletableFuture<PagedApiResponseEntity<OpinionCommentInfo>> getCommentOpinions(
            @RequestHeader Long xUserId,
            @RequestParam int page,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "DESC") String direction) {
        Long queryId = userId == null ? xUserId : userId;
        return commentService.getUserOpinionedComments(queryId, opinion, page, size, direction)
        .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping(path = "/summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> summary(@RequestParam Long postId) {
        return Flux.<ServerSentEvent<String>>create(sink -> {
            try {
                intelligenceService.summary(sink, postId);
            } catch (Exception e) {
                sink.error(e);
            }
        }).timeout(Duration.ofSeconds(300))
        .onErrorResume(e -> {
            return Flux.just(ServerSentEvent.builder("Error: " + e.getMessage()).build());
        });
    }
}
