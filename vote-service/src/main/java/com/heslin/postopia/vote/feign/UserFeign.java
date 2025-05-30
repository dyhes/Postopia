package com.heslin.postopia.vote.feign;

import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("user-service")
public interface UserFeign {
    @PostMapping(value = "user/upload/private", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadAvatar(@RequestPart("file") MultipartFile file, @RequestHeader Long xUserId);

    @Async("feignAsyncExecutor")
    @GetMapping("user/info")
    CompletableFuture<List<UserInfo>> getUserInfos(@RequestParam List<Long> userId);
}
