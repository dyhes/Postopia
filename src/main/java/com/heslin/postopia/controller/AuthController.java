package com.heslin.postopia.controller;

import com.heslin.postopia.enums.OpinionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heslin.postopia.dto.Credential;
import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.service.jwt.JWTService;
import com.heslin.postopia.service.login.LoginService;
import com.heslin.postopia.service.signup.SignupService;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SignupService signupService;
    private final LoginService loginService;
    private final JWTService jwtService;

    @Autowired
    public AuthController(SignupService signupService, LoginService loginService, JWTService jwtService) {
        this.signupService = signupService;
        this.loginService = loginService;
        this.jwtService = jwtService;
    }

    public record SignupDto(String username, String password) {}
    @PostMapping("signup")
    public BasicApiResponseEntity signup(@RequestBody SignupDto signupDto) {
        if (signupDto.username == null || signupDto.password == null) {
            throw new BadRequestException("Username and password are required");
        }
        
        Message message = signupService.signup(signupDto.username, signupDto.password);
        return BasicApiResponseEntity.ok(message);
    }

    public record LoginDto(String username, String password) {}
    @PostMapping("login")
    public ApiResponseEntity<Credential> login(@RequestBody LoginDto loginDto) {
        if (loginDto.username == null || loginDto.password == null) {
            throw new BadRequestException("Username and password are required");
        }

        try {
            Credential credential = loginService.login(loginDto.username, loginDto.password);
            return ApiResponseEntity.ok(new ApiResponse<>("用户 @" + loginDto.username + " 登录成功", credential));
        } catch (RuntimeException e) {
            return ApiResponseEntity.ok(new ApiResponse<>("用户 @" + loginDto.username + " 登录失败, " + e.getMessage(), false, null));
        }

    }

    public record RefreshDto(String refreshToken) {}

    @PostMapping("refresh")
    public ApiResponseEntity<String> refresh(@RequestBody RefreshDto refreshDto) {
        try {
            String token = jwtService.refresh(refreshDto.refreshToken);
            return ApiResponseEntity.ok(new ApiResponse<>("Refreshing token succeed", token));
        } catch (RuntimeException e) {
            return ApiResponseEntity.ok(new ApiResponse<>("Refreshing token failed, " + e.getMessage(), false, null));
        }

    }

}
