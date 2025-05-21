package com.heslin.postopia.comment.feign;


import com.heslin.postopia.post.dto.CommentPostInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("post-service")
public interface PostFeign {

    @GetMapping("post/comment")
    CompletableFuture<List<CommentPostInfo>> getCommentPostInfos(@RequestParam List<Long> ids);
}
