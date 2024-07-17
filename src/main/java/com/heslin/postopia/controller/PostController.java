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
    
    public record CreatePostDto(Long spaceId, String subject, String content) {}
    @PostMapping("create")
    public ApiResponseEntity<Long> createPost(@AuthenticationPrincipal User user, @RequestBody CreatePostDto request) {
        if (request.spaceId == null || request.subject == null || request.content == null) {
            throw new BadRequestException("spaceId, subject and content are required");
        }

        Space space = entityManager.getReference(Space.class, request.spaceId);
        var pair = postService.createPost(space, user, request.subject, request.content);
        return ApiResponseEntity.ok(new ApiResponse<>(pair.second(), pair.first()));
    }
    
    public record deletePostDto(Long postId) {}
    
    @PostMapping("delete")
    public BasicApiResponseEntity deletePost(@AuthenticationPrincipal User user, @RequestBody deletePostDto request) {
        if (request.postId == null) {
            throw new BadRequestException("postId is required");
        }

        var message = postService.deletePost(user, request.postId);
        return BasicApiResponseEntity.ok(message);
    }

    public record ReplyPostDto(Long postId, String content) {}
        
}
