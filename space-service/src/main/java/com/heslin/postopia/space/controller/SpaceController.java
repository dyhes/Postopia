package com.heslin.postopia.space.controller;

import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.space.dto.SearchSpaceInfo;
import com.heslin.postopia.space.dto.SpaceAvatar;
import com.heslin.postopia.space.dto.SpaceInfo;
import com.heslin.postopia.space.dto.VoteSpaceInfo;
import com.heslin.postopia.space.service.SpaceService;
import com.heslin.postopia.user.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("space")
public class SpaceController {
    private final SpaceService spaceService;

    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping("expel")
    public void expelUser(@RequestParam Long spaceId, @RequestParam Long userId, @RequestParam String reason) {
        spaceService.expelUser(spaceId, userId, reason);
    }

    @PostMapping("mute")
    public void muteUser(@RequestParam Long spaceId, @RequestParam Long userId, @RequestParam String reason) {
        spaceService.muteUser(spaceId, userId, reason);
    }

    @PostMapping("update")
    public void updateInfo(@RequestParam Long spaceId, @RequestParam String description, @RequestParam String avatar) {
        spaceService.updateInfo(spaceId, description, avatar);
    }

    public record SpaceCreateRequest(String name, String description){}

    @PostMapping("create")
    public ApiResponseEntity<Long> createSpace(@RequestHeader String xUsername, @RequestHeader Long xUserId, @RequestPart("info") SpaceCreateRequest info, @RequestPart(name = "avatar", required = false) MultipartFile avatar) {
        Utils.checkRequestBody(info);
        PostopiaFormatter.isValid(info.name);
        String url = null;
        if (avatar != null) {
            var avatarResponse = spaceService.uploadAvatar(avatar, xUserId);
            System.out.println("res");
            System.out.println(avatarResponse);
            System.out.println(Objects.requireNonNull(avatarResponse.getBody()).getMessage());
            if (!Objects.requireNonNull(avatarResponse.getBody()).isSuccess()) {
                return ApiResponseEntity.fail(avatarResponse.getBody().getMessage());
            }
            url = avatarResponse.getBody().getData();
        }
        try {
            Long id = spaceService.createSpace(xUsername, xUserId, info.name, info.description, url);
            return ApiResponseEntity.success(id);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    public record SpaceIdDto(Long spaceId) {}

    @PostMapping("join")
    public BasicApiResponseEntity joinSpace(@RequestHeader String xUsername, @RequestHeader Long xUserId, @RequestBody SpaceIdDto space) {
        Utils.checkRequestBody(space);
        spaceService.joinSpace(xUsername, xUserId, space.spaceId);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("leave")
    public BasicApiResponseEntity leaveSpace(@RequestHeader Long xUserId, @RequestBody SpaceIdDto space) {
        Utils.checkRequestBody(space);
        ResMessage message = spaceService.leaveSpace(xUserId, space.spaceId);
        return BasicApiResponseEntity.res(message);
    }

    @GetMapping("user/prefix")
    public PagedApiResponseEntity<UserInfo> searchMemberByPrefix(
    @RequestParam Long spaceId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam String prefix) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserInfo> members = spaceService.searchMemberByPrefix(spaceId, prefix, pageable);
        return PagedApiResponseEntity.success(members);
    }

    @GetMapping("user")
    public PagedApiResponseEntity<SpaceInfo> getUserSpaces(
        @RequestHeader Long xUserId,
        @RequestParam int page,
        @RequestParam(required = false) UserId userId,
        @RequestParam(required = false, defaultValue = "250") int size) {
        Long queryId = xUserId != null ? xUserId : userId.getId();
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(spaceService.getUserSpaces(queryId, pageable));
    }

    @GetMapping("popular")
    public PagedApiResponseEntity<SpaceInfo> getPopularSpaces(
        @RequestParam int page,
        @RequestParam(required = false, defaultValue = "250") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(spaceService.getPopularSpaces(pageable));
    }

    @GetMapping("info")
    public ApiResponseEntity<SpaceInfo> getSpaceInfo(@RequestParam(name = "id") Long spaceId) {
        SpaceInfo space = spaceService.getSpaceInfo(spaceId);
        return ApiResponseEntity.success(space);
    }

    @GetMapping("avatars")
    public ApiResponseEntity<List<SpaceAvatar>> getSpaceAvatar(@RequestParam List<Long> ids) {
        var ret = spaceService.getSpaceAvatars(ids);
        return ApiResponseEntity.success(ret);
    }

    @GetMapping("search")
    public ApiResponseEntity<List<SearchSpaceInfo>> getSearchedSpaceInfos(@RequestParam List<Long> ids) {
        var ret = spaceService.getSearchSpaceInfos(ids);
        return ApiResponseEntity.success(ret);
    }

    @GetMapping("eligible")
    public boolean isEligible(@RequestParam Long userId, @RequestParam Long spaceId) {
        return spaceService.isEligible(userId, spaceId);
    }

    @GetMapping("info/vote")
    public Pair<Boolean, VoteSpaceInfo> checkMemberForVote(Long spaceId, Long userId) {
        boolean isEligible = spaceService.isEligible(spaceId, userId);
        if (isEligible) {
            VoteSpaceInfo spaceInfo = spaceService.findVoteSpaceInfo(spaceId);
            return Pair.of(true, spaceInfo);
        }
        return Pair.of(false, new VoteSpaceInfo("null", null));
    }
}