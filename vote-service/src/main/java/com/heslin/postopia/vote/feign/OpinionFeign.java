package com.heslin.postopia.vote.feign;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("opinion-service")
public interface OpinionFeign {

    @PostMapping("opinion/vote/notify")
    void notifyVoter(@RequestParam Long voteId, @RequestParam String message);

    @Async("feignAsyncExecutor")
    @GetMapping("opinion/vote")
    CompletableFuture<List<OpinionInfo>> getOpinionInfos(@RequestParam Long userId, @RequestParam List<Long> voteId);
}
