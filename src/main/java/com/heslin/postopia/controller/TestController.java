package com.heslin.postopia.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.model.User;
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/is_authenticated")
    public BasicApiResponseEntity test(@AuthenticationPrincipal User user) {
        return BasicApiResponseEntity.ok(user + "is authenticated");
    }
        
}
