package com.heslin.postopia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.space.SpaceService;


@RestController
@RequestMapping("/space")
public class SpaceController {
    @Autowired
    private SpaceService spaceService;

    public record SpaceDto(String name, String description) {}

    @PostMapping("create")
    public BasicApiResponseEntity createSpace(@AuthenticationPrincipal User user, @RequestPart("info") SpaceDto info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        String avatarUrl = null;
        if (info.name == null || info.description == null) {
            return BasicApiResponseEntity.badRequest("space name and description are required");
        }

        if (avatar != null) {
            // object storage
        }

        Message message = spaceService.createSpace(user, info.name, info.description, avatarUrl);
        return BasicApiResponseEntity.ok(message);
    }
    
    @PostMapping("join")
    public BasicApiResponseEntity joinSpace(@AuthenticationPrincipal User user, @RequestParam("spaceId") Long spaceId) {
        if (spaceId == null) {
            return BasicApiResponseEntity.badRequest("spaceId is required");
        }
        
        Message message = spaceService.joinSpace(spaceId, user);
        return BasicApiResponseEntity.ok(message);
    }
}
