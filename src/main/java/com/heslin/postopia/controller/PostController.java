package com.heslin.postopia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.post.PostService;

import jakarta.persistence.EntityManager;



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
        
        postService.checkPostStatus(request.id);
        postService.replyPost(request.id, request.content);
        return null;
    }
}
