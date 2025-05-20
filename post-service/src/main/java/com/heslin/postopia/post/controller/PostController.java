package com.heslin.postopia.post.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heslin.postopia.common.dto.response.*;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.dto.OpinionPostInfo;
import com.heslin.postopia.post.dto.PostInfo;
import com.heslin.postopia.post.dto.UserPostInfo;
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
        return ApiResponseEntity.ok(new ApiResponse<>(pair.first(), pair.second()));
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
    public ApiResponseEntity<PostInfo> getPostInfo(@RequestParam Long id, @AuthenticationPrincipal User user) {
        if (id == null) {
            throw new BadRequestException("postId is required");
        }
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子信息成功", postService.getPostInfo(id, user)));
    }

    @GetMapping("user")
    public CompletableFuture<PagedApiResponseEntity<List<OpinionPostInfo>>> getUserPosts(
            @RequestHeader Long xUserId,
            @RequestParam int page,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        boolean isSelf = userId == null || userId.getId().equals(user.getId());
        Long queryId = userId == null ? user.getId() : userId.getId();
        Long selfId = user.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPostsByUser(isSelf, queryId, selfId, pageable))));
    }

    @GetMapping("user_opinion")
    public CompletableFuture<PagedApiResponseEntity<List<UserPostInfo>>> getUserOpinionedPosts(
    @RequestHeader Long xUserId,
    @RequestParam int page,
    @RequestParam(required = false) Long userId,
    @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
    @RequestParam(defaultValue = "50") int size,
    @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
        Long queryId = userId == null? user.getId() : userId.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子态度列表成功", new PageResult<>(postService.getPostOpinionsByUser(queryId, opinion, pageable))));
    }


    @GetMapping("popular")
    public ApiResponseEntity<PageResult<FeedPostSummary>> getPopularPosts(
    @RequestHeader Long xUserId,
    @RequestParam int page,
    @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPopularPosts(pageable, user))));
    }

    @GetMapping("space")
    public ApiResponseEntity<PageResult<SpacePostSummary>> getPosts(
    @RequestHeader Long xUserId,
    @RequestParam Long spaceId,
    @RequestParam int page,
    @RequestParam(defaultValue = "15") int size,
    @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        if (spaceId == null) {
            throw new BadRequestException("spaceId is required");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPosts(spaceId, pageable, user))));
    }

    @GetMapping("search")
    public ApiResponseEntity<List<SearchedPostInfo>> getPostInfosInSearch(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(postService.getPostInfosInSearch(ids), "success");
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
