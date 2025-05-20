package com.heslin.postopia.space.feign;

import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient("user-service")
public interface UserClient {
    @PostMapping(value = "user/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestPart("file")MultipartFile file, @RequestParam boolean isVideo, @RequestHeader Long userId);

    @GetMapping("user/search/infos")
    ResponseEntity<ApiResponse<List<UserInfo>>>  getSpaceUserInfo(@RequestParam List<Long> userId);
}
