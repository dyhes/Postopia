package com.heslin.postopia.message.controller;

import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.message.dto.MessageInfo;
import com.heslin.postopia.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("unread")
    public ApiResponseEntity<Long> getMessageCount(
    @RequestHeader Long xUserId) {
        return ApiResponseEntity.success(messageService.getMessageCount(xUserId));
    }

    @GetMapping("user")
    public PagedApiResponseEntity<MessageInfo> getMessages(
        @RequestHeader Long xUserId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size) {
            Pageable pageable = PageRequest.of(page, size);
            return PagedApiResponseEntity.success(messageService.getMessages(xUserId, pageable));
    }

    public record MessageGroupDto(List<Long> ids){}

    @PostMapping("read")
    public BasicApiResponseEntity readMessages(
    @RequestHeader Long xUserId,
    @RequestBody MessageGroupDto request) {
        Utils.checkRequestBody(request);
        messageService.readMessages(xUserId, request.ids);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("delete")
    public BasicApiResponseEntity deleteMessages(
    @RequestHeader Long xUserId,
    @RequestBody MessageGroupDto request) {
        Utils.checkRequestBody(request);
        messageService.deleteMessages(xUserId, request.ids);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("delete-read")
    public BasicApiResponseEntity deleteAllRead(
    @RequestHeader Long xUserId) {
        messageService.deleteAllRead(xUserId);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("read-all")
    public BasicApiResponseEntity readAll(
    @RequestHeader Long xUserId) {
        messageService.readAll(xUserId);
        return BasicApiResponseEntity.success();
    }
}
