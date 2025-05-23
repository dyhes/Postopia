package com.heslin.postopia.post.feign;

import com.heslin.postopia.vote.dto.VoteInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("vote-service")
public interface VoteFeign {
    @Async("feignAsyncExecutor")
    @GetMapping("vote/post")
    CompletableFuture<List<VoteInfo>> getPostVotes(@RequestParam Long userId, @RequestParam List<Long> ids);
}
