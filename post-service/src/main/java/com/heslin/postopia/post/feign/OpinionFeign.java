package com.heslin.postopia.post.feign;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("opinion-service")
public interface OpinionFeign {
    @GetMapping("opinion/post")
    CompletableFuture<List<OpinionInfo>> getOpinionInfos(@RequestParam Long userId, @RequestParam List<Long> postId);

    @GetMapping("opinion/user/post")
    Page<OpinionInfo> getUserPostOpinion(@RequestParam Long userId, @RequestParam int page, @RequestParam int size, @RequestParam OpinionStatus status);
}
