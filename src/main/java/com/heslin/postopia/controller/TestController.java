package com.heslin.postopia.controller;

import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.test.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/is_authenticated")
    public BasicApiResponseEntity test(@AuthenticationPrincipal User user) {
        return BasicApiResponseEntity.ok(user + "is authenticated");
    }

    @GetMapping("/deep")
    public BasicApiResponseEntity testDeep(@AuthenticationPrincipal User user) {
        return BasicApiResponseEntity.ok(testService.testDeep(user));
    }
}
