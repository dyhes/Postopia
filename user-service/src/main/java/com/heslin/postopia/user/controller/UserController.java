package com.heslin.postopia.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.Avatar;
import com.heslin.postopia.common.dto.SearchUserInfo;
import com.heslin.postopia.common.dto.UserId;
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

import java.util.List;

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
            return BasicApiResponseEntity.success(resMessage.message());
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
        return ApiResponseEntity.success(userService.getUserInfo(username));
    }

    @GetMapping("avatars")
    public ApiResponseEntity<List<Avatar>> getUserAvatar(@RequestParam List<String> names) {
        List<Avatar> ret = userService.getUserAvatars(names);
        return ApiResponseEntity.success(ret);
    }

    @GetMapping("search/infos")
    public ApiResponseEntity<List<SearchUserInfo>> getSearchedUserInfos(@RequestParam List<String> names) {
        List<SearchUserInfo> ret = userService.getSearchUserInfos(names);
        return ApiResponseEntity.success(ret);
    }

    public record NickNameRequest(String nickname) {}

    @PostMapping("nickname")
    public BasicApiResponseEntity updateNickName(@RequestHeader Long userId, @RequestBody NickNameRequest request) {
        Utils.checkRequestBody(request);
        PostopiaFormatter.isValid(request.nickname);
        userService.updateUserNickName(userId, request.nickname);
        return BasicApiResponseEntity.success();
    }

    public record IntroRequest(String introduction) {}

    @PostMapping("introduction")
    public BasicApiResponseEntity updateIntroduction(@RequestHeader Long userId, @RequestBody IntroRequest request) {
        Utils.checkRequestBody(request);
        userService.updateUserIntroduction(userId, request.introduction);
        return BasicApiResponseEntity.success();
    }



}
