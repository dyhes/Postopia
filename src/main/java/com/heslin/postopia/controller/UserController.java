package com.heslin.postopia.controller;

import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
