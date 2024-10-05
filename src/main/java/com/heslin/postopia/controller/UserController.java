package com.heslin.postopia.controller;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.dto.UserInfo;
import com.heslin.postopia.dto.pageresult.PageResult;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.JoinedSpaceOrder;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.post.PostService;
import com.heslin.postopia.service.space.SpaceService;
import com.heslin.postopia.service.user.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;
    private final SpaceService spaceService;
    private final PostService postService;

    @Autowired
    public UserController(UserService userService, SpaceService spaceService, PostService postService) {
        this.userService = userService;
        this.spaceService = spaceService;
        this.postService = postService;
    }

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

    public record ShowDto(boolean show) {
    }

    ;

    @PostMapping("email/show")
    public BasicApiResponseEntity switchEmailShowingState(@RequestBody ShowDto showDto, @AuthenticationPrincipal User user) {
        userService.updateShowEmail(showDto.show, user.getId());
        return BasicApiResponseEntity.ok("success");
    }

    @PostMapping("email/verify/{email}/{code}")
    public BasicApiResponseEntity verifyEmail(@PathVariable String email, @PathVariable String code, @AuthenticationPrincipal User user) {
        Message verify = userService.verifyUserEmail(email, code, user);
        return BasicApiResponseEntity.ok(verify);
    }

    @PostMapping("avatar")
    public ApiResponseEntity<String> updateAvatar(@RequestPart("avatar") MultipartFile avatar, @AuthenticationPrincipal User user) {
        try {
            String url = userService.updateUserAvatar(user.getId(), avatar);
            return ApiResponseEntity.ok(new ApiResponse<>("success", true, url));
        } catch (IOException e) {
            return ApiResponseEntity.ok(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @GetMapping("info/{maskedId}")
    public ApiResponseEntity<UserInfo> getUserInfo(@PathVariable Long maskedId) {
        return ApiResponseEntity.ok(userService.getUserInfo(User.maskId(maskedId)), "success");
    }

    @GetMapping("spaces")
    public ApiResponseEntity<PageResult<SpaceInfo>> getSpaces(@AuthenticationPrincipal User user,
                                                              @RequestParam int page,
                                                              @RequestParam(required = false, defaultValue = "250") int size,
                                                              @RequestParam(defaultValue = "LASTACTIVE") JoinedSpaceOrder order,
                                                              @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, order.getField()));
        Page<SpaceInfo> spaces = spaceService.getSpacesByUserId(user.getId(), pageable);
        return ApiResponseEntity.ok(new ApiResponse<>(null, new PageResult<>(spaces)));
    }

    @GetMapping("posts")
    public ApiResponseEntity<PageResult<PostSummary>> getPosts(
            @AuthenticationPrincipal User user,
            @RequestParam int page,
            @RequestParam(required = false, defaultValue = "50") int size,
            @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPostsByUser(user.getId(), pageable))));
    }
}
