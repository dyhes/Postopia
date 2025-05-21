package com.heslin.postopia.post.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heslin.postopia.common.dto.response.*;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.dto.*;
import com.heslin.postopia.post.request.CreatePostRequest;
import com.heslin.postopia.post.request.UpdatePostRequest;
import com.heslin.postopia.post.service.IntelligenceService;
import com.heslin.postopia.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("post")
public class PostController {
    private final PostService postService;
    private final IntelligenceService intelligenceService;

    @Autowired
    public PostController(PostService postService, IntelligenceService intelligenceService) {
        this.postService = postService;
        this.intelligenceService = intelligenceService;
    }

    @GetMapping(value = "summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> summary(@RequestParam Long postId) {
        return Flux.create(sink -> {
            CompletableFuture.runAsync(() -> {
                try {
                    intelligenceService.summary(sink, postId);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    @PostMapping("update")
    public BasicApiResponseEntity updatePost(@RequestHeader Long xUserId, @RequestBody UpdatePostRequest request) {
        Utils.checkRequestBody(request);
        postService.validate(xUserId, request.spaceId());
        postService.updatePost(xUserId, request);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createPost(@RequestHeader Long xUserId, @RequestBody CreatePostRequest request) {
        Utils.checkRequestBody(request);
        postService.validate(xUserId, request.spaceId());
        Long pid = postService.createPost(xUserId, request);
        return ApiResponseEntity.success(pid);
    }

    @PostMapping("delete")
    private void deletePost(@RequestParam Long postId, @RequestParam Long spaceId, @RequestParam Long userId) {
        postService.deletePost(postId, spaceId, userId);
    }

    @GetMapping("archive")
    public boolean checkPostArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived) {
        return postService.checkPostArchiveStatus(postId, isArchived);
    }

    @PostMapping("archive")
    public  void updateArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived) {
        postService.updateArchiveStatus(postId, isArchived);
    }

    @GetMapping("info")
    public CompletableFuture<ApiResponseEntity<PostInfo>> getPostInfo(@RequestHeader Long xUserId, @RequestParam Long postId) {
        return postService.getPostInfo(xUserId, postId).thenApply(ApiResponseEntity::success);
    }

    @GetMapping("space")
    public CompletableFuture<PagedApiResponseEntity<PostInfo>> getPosts(
        @RequestHeader Long xUserId,
        @RequestParam Long spaceId,
        @RequestParam int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        return postService.getSpacePosts(xUserId, spaceId, pageable)
            .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("user")
    public CompletableFuture<PagedApiResponseEntity<UserPostInfo>> getUserPosts(
            @RequestHeader Long xUserId,
            @RequestParam int page,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        Long queryId = userId == null ? xUserId : userId;
        return postService.getUserPosts(xUserId, queryId, pageable)
            .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("user/opinion")
    public CompletableFuture<PagedApiResponseEntity<OpinionPostInfo>> getUserOpinionedPosts(
        @RequestHeader Long xUserId,
        @RequestParam int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
        @RequestParam(defaultValue = "DESC") String direction) {
        Long queryId = userId == null? xUserId : userId;
        return postService.getUserOpinionedPosts(queryId, opinion, page, size, direction)
            .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("popular")
    public CompletableFuture<PagedApiResponseEntity<FeedPostInfo>> getPopularPosts(
    @RequestHeader Long xUserId,
    @RequestParam int page,
    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.getPopularPosts(xUserId, pageable)
            .thenApply(PagedApiResponseEntity::success);
    }

    @GetMapping("search")
    public CompletableFuture<ApiResponseEntity<List<FeedPostInfo>>> getPostInfosInSearch(@RequestHeader Long xUserId, @RequestParam List<Long> ids) {
        return postService.getSearchPosts(xUserId, ids).thenApply(ApiResponseEntity::success);
    }

    @GetMapping("comment")
    public List<CommentPostInfo> getCommentPostInfos(@RequestParam List<Long> ids) {
        return postService.getCommentPostInfos(ids);
    }

//    @PostMapping("draft")
//    public BasicApiResponseEntity draftPost(@RequestHeader Long xUserId, @RequestBody PostDraftRequest request) {
//        if (request.spaceId == null || request.subject == null || request.content == null) {
//            throw new BadRequestException();
//        }
//        Space space = Space.builder().id(request.spaceId).build();
//        try {
//            boolean success = postService.draftPost(space, user, request.subject, request.content, request.id);
//            return BasicApiResponseEntity.ok(success);
//        } catch (DataIntegrityViolationException e) {
//            throw new BadRequestException("draft post failed, please check the spaceId");
//        }
//    }
//
//    @PostMapping("draft-delete")
//    public BasicApiResponseEntity deleteDraft(@RequestHeader Long xUserId, @RequestBody PostIdDto request) {
//        if (request.id == null) {
//            throw new BadRequestException("postId is required");
//        }
//        boolean success = postService.deleteDraft(request.id, user.getId());
//        return BasicApiResponseEntity.ok(success? "草稿删除成功" : "草稿不存在", success);
//    }
//
//    @GetMapping("draft-list")
//    public PagedApiResponseEntity<PostDraftDto> getPosts(
//    @RequestHeader Long xUserId,
//    @RequestParam(defaultValue = "0")  int page,
//    @RequestParam(defaultValue = "20") int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "d.updatedAt"));
//        return PagedApiResponseEntity.ok(postService.getPostDrafts(user, pageable));
//        }
}
