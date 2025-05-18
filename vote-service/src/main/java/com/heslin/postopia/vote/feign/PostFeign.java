package com.heslin.postopia.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("post-service")
public interface PostFeign {

    @PostMapping("post/delete")
    void deletePost(@RequestParam Long postId, @RequestParam Long spaceId, @RequestParam Long userId);

    @GetMapping("post/archive")
    boolean checkPostArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived);

    @PostMapping("post/archive")
    void updateArchiveStatus(@RequestParam Long postId, @RequestParam boolean isArchived);
}
