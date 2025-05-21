package com.heslin.postopia.comment.feign;

import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("user-service")
public interface UserFeign {
    @GetMapping("user/info")
    CompletableFuture<List<UserInfo>> getUserInfos(@RequestParam List<Long> userId);
}
