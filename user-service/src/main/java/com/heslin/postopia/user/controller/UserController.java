package com.heslin.postopia.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.user.dto.Credential;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.request.RefreshRequest;
import com.heslin.postopia.user.request.SignInRequest;
import com.heslin.postopia.user.request.SignUpRequest;
import com.heslin.postopia.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("auth/signup")
    public BasicApiResponseEntity signup(@RequestBody SignUpRequest signupRequest) {
        Utils.checkRequestBody(signupRequest);
        PostopiaFormatter.isValid(signupRequest.username());
        ResMessage resMessage = userService.signup(signupRequest);
        if (resMessage.success()) {
            return BasicApiResponseEntity.sucess(resMessage.message());
        } else {
            return BasicApiResponseEntity.fail(resMessage.message());
        }
    }

    @PostMapping("auth/login")
    public ApiResponseEntity<Credential> login(@RequestBody SignInRequest signInRequest) {
        Utils.checkRequestBody(signInRequest);
        try {
            Credential credential = userService.signin(signInRequest);
            return ApiResponseEntity.success(credential, "用户 @" + signInRequest.username() + " 登录成功");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail("用户 @" + signInRequest.username() + " 登录失败, " + e.getMessage());
        }
    }

    @PostMapping("auth/refresh")
    public ApiResponseEntity<String> refresh(@RequestBody RefreshRequest refreshRequest) {
        try {
            String token = userService.refresh(refreshRequest);
            return ApiResponseEntity.success(token);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail();
        }
    }

    @GetMapping("info/{username}")
    public ApiResponseEntity<UserInfo> getUserInfo(@PathVariable String username) {
        System.out.println("username");
        try {
            String id = new ObjectMapper().writeValueAsString(new UserId(1L));
            System.out.println(id);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        }
        return ApiResponseEntity.success(userService.getUserInfo(username));
    }
    
}
