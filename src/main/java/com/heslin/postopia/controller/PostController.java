package com.heslin.postopia.controller;

import com.heslin.postopia.dto.pageresult.PageResult;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.post.PostService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("post")
public class PostController {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PostService postService;
    
    public record CreatePostDto(Long spaceId, String subject, String content, boolean isDraft) {}

    public record PostIdDto(Long id) {}

    public record UpdatePostDto(Long id, String subject, String content) {}

    @PostMapping("update")
    public BasicApiResponseEntity updatePost(@AuthenticationPrincipal User user, @RequestBody UpdatePostDto request) {
        if (request.id == null || request.subject == null || request.content == null) {
            throw new BadRequestException("postId, subject and content are required");
        }

        postService.authorize(user, request.id);
        postService.updatePost(request.id, request.subject, request.content);
        return BasicApiResponseEntity.ok("帖子更新成功");
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createPost(@AuthenticationPrincipal User user, @RequestBody CreatePostDto request) {
        if (request.spaceId == null || request.subject == null || request.content == null) {
            throw new BadRequestException("spaceId, subject and content are required");
        }

        Space space = entityManager.getReference(Space.class, request.spaceId);
        var pair = postService.createPost(request.isDraft, space, user, request.subject, request.content);
        return ApiResponseEntity.ok(new ApiResponse<>(pair.first(), pair.second()));
    }

    @PostMapping("archive")
    public BasicApiResponseEntity archivePost(@AuthenticationPrincipal User user, @RequestBody PostIdDto request) {
        if (request.id == null) {
            throw new BadRequestException("postId is required");
        }

        postService.authorize(user, request.id);
        postService.archivePost(request.id);
        return BasicApiResponseEntity.ok("帖子归档成功");
    }

    @PostMapping("unArchive")
    public BasicApiResponseEntity unArchivePost(@AuthenticationPrincipal User user, @RequestBody PostIdDto request) {
        if (request.id == null) {
            throw new BadRequestException("postId is required");
        }

        postService.authorize(user, request.id);
        postService.unarchivePost(request.id);
        return BasicApiResponseEntity.ok("帖子取消归档成功");
    }
    
    @PostMapping("delete")
    public BasicApiResponseEntity deletePost(@AuthenticationPrincipal User user, @RequestBody PostIdDto request) {
        if (request.id == null) {
            throw new BadRequestException("postId is required");
        }

        postService.authorize(user, request.id);
        postService.deletePost(request.id);
        return BasicApiResponseEntity.ok("帖子删除成功");
    }

    public record ReplyPostDto(Long id, String content) {}
    
    @PostMapping("reply")
    public BasicApiResponseEntity replyPost(@AuthenticationPrincipal User user, @RequestBody ReplyPostDto request) {
        if (request.id == null || request.content == null) {
            throw new BadRequestException("postId and content are required");
        }
        
        //postService.checkPostStatus(request.id);
        postService.replyPost(request.id, request.content);
        return BasicApiResponseEntity.ok("回复成功");
    }

    @GetMapping("info")
    public ApiResponseEntity<PostInfo> getPostInfo(@RequestParam Long id) {
        if (id == null) {
            throw new BadRequestException("postId is required");
        }
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子信息成功", postService.getPostInfo(id, null)));
    }

    public record SpaceIdDto(Long id) {}

    @GetMapping("list")
    public ApiResponseEntity<PageResult<PostSummary>> getPosts(
            @AuthenticationPrincipal User user,
            @RequestParam Long id,
            @RequestParam int page,
            @RequestParam(required = false, defaultValue = "50") int size,
            @RequestParam(defaultValue = "desc") String direction) {
        if (id == null) {
            throw new BadRequestException("spaceId is required");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPosts(id, pageable, user))));
    }

    @PostMapping("like")
    public BasicApiResponseEntity likePost(@RequestBody PostIdDto dto) {
        if (dto.id == null) {
            throw new BadRequestException("postId is required");
        }
        postService.likePost(dto.id, null);
        return BasicApiResponseEntity.ok("post liked!");
    }

    @PostMapping("dislike")
    public BasicApiResponseEntity disLikePost(@RequestBody PostIdDto dto) {
        if (dto.id == null) {
            throw new BadRequestException("postId is required");
        }
        postService.disLikePost(dto.id, null);
        return BasicApiResponseEntity.ok("post disliked!");
    }
}
