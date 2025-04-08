package com.heslin.postopia.controller;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.jpa.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/is_authenticated")
    public BasicApiResponseEntity test(@AuthenticationPrincipal User user) {
        String s = "User " + new UserId(user.getId()) + " ";
        s += user.getUsername();
        s += " is authenticated";
        return BasicApiResponseEntity.ok(s);
    }

    @GetMapping("/user_id")
    public ApiResponseEntity<UserId> testUserId(@AuthenticationPrincipal User user) {
        return BasicApiResponseEntity.ok(new ApiResponse<>("success", new UserId(user.getId())));
    }
}
