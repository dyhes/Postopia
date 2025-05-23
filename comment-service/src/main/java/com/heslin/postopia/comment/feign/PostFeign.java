package com.heslin.postopia.comment.feign;


import com.heslin.postopia.post.dto.CommentPostInfo;
import com.heslin.postopia.post.dto.SummaryPostInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("post-service")
public interface PostFeign {
    @Async("feignAsyncExecutor")
    @GetMapping("post/comment")
    CompletableFuture<List<CommentPostInfo>> getCommentPostInfos(@RequestParam List<Long> ids);

    @GetMapping("post/summary")
    SummaryPostInfo getSummaryPostInfo(@RequestParam Long postId);
}
