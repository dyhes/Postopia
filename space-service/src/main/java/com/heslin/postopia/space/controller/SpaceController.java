package com.heslin.postopia.space.controller;

import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.space.feign.UserClient;
import com.heslin.postopia.space.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("space")
public class SpaceController {
    private final SpaceService spaceService;
    private final UserClient userClient;

    @Autowired
    public SpaceController(SpaceService spaceService, UserClient userClient) {
        this.spaceService = spaceService;
        this.userClient = userClient;
    }

//    public record SpaceDto(String name, String description) {
//    }
//
//    @GetMapping("")
//    public PagedApiResponseEntity<UserSummary> searchMemberByPrefix(
//    @RequestParam String spaceName,
//    @RequestParam(defaultValue = "0") int page,
//    @RequestParam(defaultValue = "20") int size,
//    @RequestParam String prefix) {
//        if (prefix == null || prefix.isBlank()) {
//            throw new BadRequestException("prefix is required");
//        }
//        Pageable pageable = PageRequest.of(page, size);
//        Page<UserSummary> users = spaceService.searchUserByPrefix(spaceName, prefix, pageable);
//        return PagedApiResponseEntity.ok(users);
//    }

    public record SpaceCreateRequest(String name, String description){}

    @PostMapping("create")
    public ApiResponseEntity<Long> createSpace(@RequestHeader String username, @RequestHeader Long userId, @RequestPart("info") SpaceCreateRequest info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        Utils.checkRequestBody(info);
        PostopiaFormatter.isValid(info.name);
        String url = null;
        if (avatar != null) {
            var avatarResponse = userClient.uploadAvatar(avatar, false, userId);
            System.out.println("res");
            System.out.println(avatarResponse);
            System.out.println(Objects.requireNonNull(avatarResponse.getBody()).getMessage());
            if (!Objects.requireNonNull(avatarResponse.getBody()).isSuccess()) {
                return ApiResponseEntity.fail(avatarResponse.getBody().getMessage());
            }
            url = avatarResponse.getBody().getData();
        }
        try {
            Long id = spaceService.createSpace(username, userId, info.name, info.description, url);
            return ApiResponseEntity.success(id);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("join")
    public BasicApiResponseEntity joinSpace(@RequestHeader String username, @RequestHeader Long userId, @RequestBody SpaceIdDto space) {
        Utils.checkRequestBody(space);
        spaceService.joinSpace(username, userId, space.spaceId);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("leave")
    public BasicApiResponseEntity leaveSpace(@RequestHeader Long userId, @RequestBody SpaceIdDto space) {
        Utils.checkRequestBody(space);
        spaceService.leaveSpace(userId, space.spaceId);
        return BasicApiResponseEntity.success();
    }

    public record SpaceIdDto(Long spaceId) {}


//
//    @GetMapping("list")
//    public ApiResponseEntity<PageResult<SpaceInfo>> getPopularSpaces(@@RequestHeader  Long userId,
//                                                                     @RequestParam int page,
//                                                                     @RequestParam(required = false, defaultValue = "250") int size,
//                                                                     @RequestParam(defaultValue = "MEMBERCOUNT")PopularSpaceOrder order) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<SpaceInfo> spaces = spaceService.getPopularSpaces(order, pageable);
//        return ApiResponseEntity.ok(new ApiResponse<>(null,new PageResult<>(spaces)));
//    }
//
//    @GetMapping("info")
//    public ApiResponseEntity<SpaceInfo> getSpaceInfo(@RequestParam(name = "id") Long spaceId) {
//        SpaceInfo space = spaceService.getSpaceInfo(spaceId);
//        return ApiResponseEntity.ok(space, "success");
//    }
//
//    @GetMapping("avatars")
//    public ApiResponseEntity<List<Avatar>> getSpaceAvatar(@RequestParam List<String> names) {
//        var ret = spaceService.getSpaceAvatars(names);
//        return ApiResponseEntity.ok(ret, "success");
//    }
//
//    @GetMapping("search-info")
//    public ApiResponseEntity<List<SearchedSpaceInfo>> getSearchedSpaceInfos(@RequestParam List<String> names) {
//        var ret = spaceService.getSearchedSpaceInfos(names);
//        return ApiResponseEntity.ok(ret, "success");
//    }
}