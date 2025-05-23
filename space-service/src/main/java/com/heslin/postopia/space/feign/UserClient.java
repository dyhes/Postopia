package com.heslin.postopia.space.feign;

import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient("user-service")
public interface UserClient {
    @PostMapping(value = "user/upload/private", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadAvatar(@RequestPart("file")MultipartFile file, @RequestHeader Long xUserId);

    @GetMapping("user/info")
    List<UserInfo>  getSpaceUserInfo(@RequestParam List<Long> userId);
}
