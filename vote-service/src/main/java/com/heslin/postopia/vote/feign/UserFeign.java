package com.heslin.postopia.vote.feign;

import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FeignClient("user-service")
public interface UserFeign {
    @PostMapping(value = "user/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestPart("file") MultipartFile file, @RequestParam boolean isVideo, @RequestHeader Long userId);

    @Async("feignAsyncExecutor")
    @GetMapping("user/info")
    CompletableFuture<List<UserInfo>> getUserInfos(@RequestParam List<Long> userId);
}
