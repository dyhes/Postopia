package com.heslin.postopia.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("comment-service")
public interface CommentFeign {
    @PostMapping("comment/delete")
    void deleteComment(@RequestParam Long spaceId, @RequestParam Long postId, @RequestParam Long commentId, @RequestParam Long userId);

    @GetMapping("comment/pin")
    boolean checkPinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);

    @PostMapping("comment/pin")
    void updatePinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);
}

