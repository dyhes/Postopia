package com.heslin.postopia.controller;

import com.heslin.postopia.dto.PageResult;
import com.heslin.postopia.dto.post.PostDraftDto;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
import com.heslin.postopia.dto.post.SpacePostSummary;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.post.PostService;
import com.heslin.postopia.util.Utils;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("post")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    public record CreatePostDto(Long spaceId, String subject, String content, String spaceName) {}

    public record PostIdDto(Long id) {}

    public record UpdatePostDto(Long id, String subject, String content, String spaceName) {}

    @PostMapping("update")
    public BasicApiResponseEntity updatePost(@AuthenticationPrincipal User user, @RequestBody UpdatePostDto request) {
        Utils.checkRequestBody(request);
        boolean success = postService.updatePost(request.id, user.getId(), request.spaceName, request.subject, request.content);
        return BasicApiResponseEntity.ok(success);
    }

    public record PostDraftRequest(Long id, String subject, String content, Long spaceId){}

    @PostMapping("draft")
    public BasicApiResponseEntity draftPost(@AuthenticationPrincipal User user, @RequestBody PostDraftRequest request) {
        if (request.spaceId == null || request.subject == null || request.content == null) {
            throw new BadRequestException();
        }
        Space space = Space.builder().id(request.spaceId).build();
        try {
            boolean success = postService.draftPost(space, user, request.subject, request.content, request.id);
            return BasicApiResponseEntity.ok(success);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("draft post failed, please check the spaceId");
        }
    }

    @PostMapping("draft-delete")
    public BasicApiResponseEntity deleteDraft(@AuthenticationPrincipal User user, @RequestBody PostIdDto request) {
        if (request.id == null) {
            throw new BadRequestException("postId is required");
        }
        boolean success = postService.deleteDraft(request.id, user.getId());
        return BasicApiResponseEntity.ok(success? "草稿删除成功" : "草稿不存在", success);
    }

    @GetMapping("draft-list")
    public PagedApiResponseEntity<PostDraftDto> getPosts(
    @AuthenticationPrincipal User user,
    @RequestParam(defaultValue = "0")  int page,
    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "d.updatedAt"));
        return PagedApiResponseEntity.ok(postService.getPostDrafts(user, pageable));
        }

    @PostMapping("create")
    public ApiResponseEntity<Long> createPost(@AuthenticationPrincipal User user, @RequestBody CreatePostDto request) {
        Utils.checkRequestBody(request);
        Space space = Space.builder()
                    .id(request.spaceId)
                    .name(request.spaceName)
                    .build();
        var pair = postService.createPost(space, user, request.subject, request.content);
        return ApiResponseEntity.ok(new ApiResponse<>(pair.first(), pair.second()));
    }

    public record ReplyPostDto(Long postId, String content, String spaceName, String replyUser) {}
    
    @PostMapping("reply")
    public ApiResponseEntity<Long> replyPost(@AuthenticationPrincipal User user, @RequestBody ReplyPostDto request) {
        Utils.checkRequestBody(request);
        Post post = Post.builder().id(request.postId).build();
        Space space = Space.builder().name(request.spaceName).build();
        //postService.checkPostStatus(request.id);
        Comment comment = postService.replyPost(post, request.content, user, space, request.replyUser);
        return BasicApiResponseEntity.ok(comment.getId(), "回复成功", true);
    }

    @GetMapping("info")
    public ApiResponseEntity<PostInfo> getPostInfo(@RequestParam Long id, @AuthenticationPrincipal User user) {
        if (id == null) {
            throw new BadRequestException("postId is required");
        }
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子信息成功", postService.getPostInfo(id, user)));
    }


    @GetMapping("list")
    public ApiResponseEntity<PageResult<SpacePostSummary>> getPosts(
            @AuthenticationPrincipal User user,
            @RequestParam Long spaceId,
            @RequestParam int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        if (spaceId == null) {
            throw new BadRequestException("spaceId is required");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPosts(spaceId, pageable, user))));
    }

    // 非幂等！！！
    public record PostOpinionDto(Long id, String spaceName, boolean isPositive){}
    @PostMapping("opinion")
    public BasicApiResponseEntity upsertPostOpinion(@RequestBody PostOpinionDto request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        postService.upsertPostOpinion(user, request.id, request.spaceName, request.isPositive);
        return BasicApiResponseEntity.ok("success");
    }

    public record DeletePostOpinionDto(Long id, boolean isPositive){}
    @PostMapping("opinion-delete")
    public BasicApiResponseEntity deletePostOpinion(@RequestBody DeletePostOpinionDto request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        boolean success = postService.deletePostOpinion(user, request.id, request.isPositive);
        return BasicApiResponseEntity.ok(success);
    }

    @GetMapping("search-info")
    public ApiResponseEntity<List<SearchedPostInfo>> getPostInfosInSearch(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(postService.getPostInfosInSearch(ids), "success");
    }
}
