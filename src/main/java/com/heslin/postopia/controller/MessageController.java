package com.heslin.postopia.controller;

import com.heslin.postopia.dto.UserMessage;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.message.MessageService;
import com.heslin.postopia.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("message")
public class MessageController {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("list")
    public PagedApiResponseEntity<UserMessage> getMessages(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
            Pageable pageable = PageRequest.of(page, size);
            return PagedApiResponseEntity.ok(messageService.getMessages(user, pageable));
    }

    public record MessageGroupDto(List<Long> ids){}

    @PostMapping("read")
    public BasicApiResponseEntity readMessages(
    @AuthenticationPrincipal User user,
    @RequestBody MessageGroupDto request) {
        Utils.checkRequestBody(request);
        messageService.readMessages(user, request.ids);
        return BasicApiResponseEntity.ok(true);
    }

    @PostMapping("delete")
    public BasicApiResponseEntity deleteMessages(
    @AuthenticationPrincipal User user,
    @RequestBody MessageGroupDto request) {
        Utils.checkRequestBody(request);
        messageService.deleteMessages(user, request.ids);
        return BasicApiResponseEntity.ok(true);
    }

    @PostMapping("delete-read")
    public BasicApiResponseEntity deleteAllRead(
    @AuthenticationPrincipal User user) {
        messageService.deleteAllRead(user);
        return BasicApiResponseEntity.ok(true);
    }

    @PostMapping("read-all")
    public BasicApiResponseEntity readAll(
    @AuthenticationPrincipal User user) {
        messageService.readAll(user);
        return BasicApiResponseEntity.ok(true);
    }
}
