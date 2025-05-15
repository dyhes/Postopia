package com.heslin.postopia.user.controller;

import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public record SignupRequest(String username, String password) {}
    @PostMapping("/auth/signup")
    public BasicApiResponseEntity signup(@RequestBody SignupRequest signupRequest) {
        Utils.checkRequestBody(signupRequest);
        PostopiaFormatter.isValid(signupRequest.username);
        ResMessage resMessage = userService.signup(signupRequest);
        return BasicApiResponseEntity.OK(resMessage);
    }
    
}
