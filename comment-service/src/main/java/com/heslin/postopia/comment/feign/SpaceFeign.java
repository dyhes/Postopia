package com.heslin.postopia.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("space-service")
public interface SpaceFeign {
    @GetMapping("space/eligible")
    boolean isEligible(@RequestParam Long userId, @RequestParam Long spaceId);
}
