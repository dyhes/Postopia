package com.heslin.postopia.post.controller;

import com.heslin.postopia.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("post")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("post/delete")
    private void deletePost(@RequestParam Long postId, @RequestParam Long spaceId, @RequestParam Long userId) {
        postService.deletePost(postId, spaceId, userId);
    }

    @GetMapping("post/archive")
    public boolean checkPostArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived) {
        return postService.checkPostArchiveStatus(postId, isArchived);
    }

    @PostMapping("post/archive")
    public  void updateArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived) {
        postService.updateArchiveStatus(postId, isArchived);
    }
}
