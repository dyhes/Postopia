package com.heslin.postopia.user.controller;
import com.heslin.postopia.common.dto.xUserId;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.user.dto.Credential;
import com.heslin.postopia.user.dto.SearchUserInfo;
import com.heslin.postopia.user.dto.UserAvatar;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.request.RefreshRequest;
import com.heslin.postopia.user.request.SignInRequest;
import com.heslin.postopia.user.request.SignUpRequest;
import com.heslin.postopia.user.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
        return BasicApiResponseEntity.res(resMessage);
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

    @GetMapping("info/{xUserId}")
    public ApiResponseEntity<UserInfo> getUserInfo(@PathVariable xUserId xUserId) {
        return ApiResponseEntity.success(userService.getUserInfo(xUserId.getId()));
    }

    @GetMapping("avatars")
    public ApiResponseEntity<List<UserAvatar>> getUserAvatar(@RequestParam List<xUserId> ids) {
        List<UserAvatar> ret = userService.getUserAvatars(ids.stream().map(xUserId::getId).collect(Collectors.toList()));
        return ApiResponseEntity.success(ret);
    }

    @GetMapping("search/infos")
    public ApiResponseEntity<List<SearchUserInfo>> getSearchedUserInfos(@RequestParam List<xUserId> ids) {
        List<SearchUserInfo> ret = userService.getSearchUserInfos(ids.stream().map(xUserId::getId).collect(Collectors.toList()));
        return ApiResponseEntity.success(ret);
    }

    @PostMapping("upload")
    public ApiResponseEntity<String> uploadAsset(@RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "false") boolean isVideo, @RequestHeader Long xUserId) {
        try {
            String url = userService.uploadAsset(new xUserId(xUserId), file, isVideo);
            return ApiResponseEntity.success(url);
        } catch (IOException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("avatar")
    public ApiResponseEntity<String> updateAvatar(@RequestPart("avatar") MultipartFile avatar, @RequestHeader Long xUserId) {
        try {
            String url = userService.uploadAsset(new xUserId(xUserId), avatar, false);
            userService.updateUserAvatar(xUserId, url);
            return ApiResponseEntity.success(url);
        } catch (IOException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    public record NickNameRequest(String nickname) {}

    @PostMapping("nickname")
    public BasicApiResponseEntity updateNickName(@RequestHeader Long xUserId, @RequestHeader String username, @RequestBody NickNameRequest request) {
        Utils.checkRequestBody(request);
        PostopiaFormatter.isValid(request.nickname);
        userService.updateUserNickName(xUserId, username, request.nickname);
        return BasicApiResponseEntity.success();
    }

    public record IntroRequest(String introduction) {}

    @PostMapping("introduction")
    public BasicApiResponseEntity updateIntroduction(@RequestHeader Long xUserId, @RequestBody IntroRequest request) {
        Utils.checkRequestBody(request);
        userService.updateUserIntroduction(xUserId, request.introduction);
        return BasicApiResponseEntity.success();
    }

    public record ShowEmailRequest(boolean show) {}

    @PostMapping("email/show")
    public BasicApiResponseEntity switchEmailShowingState(@RequestBody ShowEmailRequest showEmailRequest, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(showEmailRequest);
        userService.updateUserShowEmail(xUserId, showEmailRequest.show);
        return BasicApiResponseEntity.success();
    }

    public record EmailRequest(String email) {}

    @PostMapping("email")
    public BasicApiResponseEntity updateEmail(@RequestBody EmailRequest emailRequest, @RequestHeader String username, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(emailRequest);
        try {
            userService.updateUserEmail(xUserId, username, emailRequest.email);
        } catch (MessagingException e) {
            return BasicApiResponseEntity.fail(e.getMessage());
        }
        ;
        return BasicApiResponseEntity.success();
    }

    public record VerifyEmailRequest(String email, String authCode){};

    @PostMapping("email/verify")
    public BasicApiResponseEntity verifyEmail(@RequestBody VerifyEmailRequest verifyEmailRequest , @RequestHeader Long xUserId) {
        ResMessage verify = userService.verifyUserEmail(xUserId, verifyEmailRequest.email, verifyEmailRequest.authCode);
        return BasicApiResponseEntity.res(verify);
    }

}
