package com.heslin.postopia.controller;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.dto.pageresult.PageResult;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.os.OSService;
import com.heslin.postopia.service.space.SpaceService;
import com.heslin.postopia.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/space")
public class SpaceController {
    private final SpaceService spaceService;

    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public record SpaceDto(String name, String description) {
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createSpace(@AuthenticationPrincipal User user, @RequestPart("info") SpaceDto info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        String avatarUrl = null;
        if (info.name == null || info.description == null) {
            throw new BadRequestException("space name and description are required");
        }

        Pair<Message, Long> pair = spaceService.createSpace(user, info.name, info.description, avatar);

        return ApiResponseEntity.ok(new ApiResponse<>(pair.second(), pair.first()));
    }

    public record SpaceIdDto(Long spaceId) {
    }

    ;

    @PostMapping("join")
    public BasicApiResponseEntity joinSpace(@AuthenticationPrincipal User user, @RequestBody SpaceIdDto space) {
        System.out.println("here1");
        System.out.println(space);
        if (space.spaceId == null) {
            System.out.println("here2");
            return BasicApiResponseEntity.badRequest("spaceId is required");
        }

        Message message = spaceService.joinSpace(space.spaceId, user);
        return BasicApiResponseEntity.ok(message);
    }

    @GetMapping("list")
    public ApiResponseEntity<PageResult<SpaceInfo>> getPopularSpaces(@AuthenticationPrincipal User user, 
    @RequestParam int page, 
    @RequestParam(required = false, defaultValue = "250") int size, 
    @RequestParam(defaultValue = "MEMBERCOUNT")PopularSpaceOrder order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SpaceInfo> spaces = spaceService.getPopularSpaces(order, pageable);
        return ApiResponseEntity.ok(new ApiResponse<>(null,new PageResult<>(spaces)));
    }

    @PostMapping("leave")
        public BasicApiResponseEntity leaveSpace(@AuthenticationPrincipal User user, @RequestBody SpaceIdDto space) {
        System.out.println("here1");
        if (space.spaceId == null) {
            System.out.println("here2");
                return BasicApiResponseEntity.badRequest("spaceId is required");
            }

        System.out.println("here3");
        Message message = spaceService.leaveSpace(space.spaceId, user);
            return BasicApiResponseEntity.ok(message);
        }
        
}
