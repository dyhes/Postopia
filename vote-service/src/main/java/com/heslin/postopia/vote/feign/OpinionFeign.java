package com.heslin.postopia.vote.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("opinion-service")
public interface OpinionFeign {

    @PostMapping("/opinion/vote/notify")
    void notifyVoter(@RequestParam Long voteId, @RequestParam String message);
}
