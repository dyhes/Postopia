package com.heslin.postopia.controller;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.user.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    public record NickNameDto(Long id, String nickname) {
    }

    ;

    @PostMapping("nickname")
    public BasicApiResponseEntity updateNickName(@RequestBody NickNameDto dto) {
        if (dto.id == null || dto.nickname == null) {
            throw new BadRequestException();
        }
        userService.updateUserNickName(dto.id, dto.nickname);
        return BasicApiResponseEntity.ok("succeed!");
    }

    public record EmailDto(String email) {
    }

    @PostMapping("email/request/{email}")
    public BasicApiResponseEntity updateEmail(@PathVariable String email, @AuthenticationPrincipal User user) {
        if (email == null) {
            throw new BadRequestException();
        }
        try {
            userService.updateUserEmail(email, user);
        } catch (MessagingException e) {
            return BasicApiResponseEntity.ok(e.getMessage(), false);
        }
        ;
        return BasicApiResponseEntity.ok("mail succeed!");
    }

    @PostMapping("email/verify/{email}/{code}")
    public BasicApiResponseEntity verifyEmail(@PathVariable String email, @PathVariable String code, @AuthenticationPrincipal User user) {
        Message verify = userService.verifyUserEmail(email, code, user);
        return BasicApiResponseEntity.ok(verify);
    }

}
