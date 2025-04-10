package com.heslin.postopia.controller;

import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.dto.PageResult;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.response.ApiResponse;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.enums.JoinedSpaceOrder;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.service.post.PostService;
import com.heslin.postopia.service.space.SpaceService;
import com.heslin.postopia.service.user.UserService;
import com.heslin.postopia.util.Utils;
import jakarta.mail.MessagingException;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;
    private final SpaceService spaceService;
    private final PostService postService;
    private final CommentService commentService;

    @Autowired
    public UserController(UserService userService, SpaceService spaceService, PostService postService, CommentService commentService) {
        this.userService = userService;
        this.spaceService = spaceService;
        this.postService = postService;
        this.commentService = commentService;
    }

    public record NickNameDto(String nickname) {}

    @PostMapping("nickname")
    public BasicApiResponseEntity updateNickName(@AuthenticationPrincipal User user, @RequestBody NickNameDto dto) {
        Utils.checkRequestBody(dto);
        userService.updateUserNickName(user, dto.nickname);
        return BasicApiResponseEntity.ok("succeed!");
    }

    public record EmailDto(String email) {}

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

    public record ShowDto(boolean show) {}

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
            String url = userService.updateUserAvatar(new UserId(user.getId()), avatar);
            return ApiResponseEntity.ok(new ApiResponse<>("success", true, url));
        } catch (IOException e) {
            return ApiResponseEntity.ok(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @PostMapping("upload")
    public ApiResponseEntity<String> uploadImg(@RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "false") boolean isVideo, @AuthenticationPrincipal User user) {
        try {
            String url = userService.uploadFile(new UserId(user.getId()), file, isVideo);
            return ApiResponseEntity.ok(new ApiResponse<>("success", true, url));
        } catch (IOException e) {
            return ApiResponseEntity.ok(new ApiResponse<>(e.getMessage(), false, null));
        }
    }

    @GetMapping("info/{userId}")
    public ApiResponseEntity<UserInfo> getUserInfo(@PathVariable UserId userId) {
        return ApiResponseEntity.ok(userService.getUserInfo(userId.getId()), "success");
    }

    @GetMapping("spaces")
    public ApiResponseEntity<PageResult<SpaceInfo>> getSpaces(@AuthenticationPrincipal User user,
                                                              @RequestParam int page,
                                                              @RequestParam(required = false) UserId userId,
                                                              @RequestParam(required = false, defaultValue = "250") int size,
                                                              @RequestParam(defaultValue = "LASTACTIVE") JoinedSpaceOrder order,
                                                              @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Long queryId = userId == null ? user.getId() : userId.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, order.getField()));
        Page<SpaceInfo> spaces = spaceService.getSpacesByUserId(queryId, pageable);
        return ApiResponseEntity.ok(new ApiResponse<>(null, new PageResult<>(spaces)));
    }

    @GetMapping("posts")
    public ApiResponseEntity<PageResult<PostSummary>> getPosts(
            @AuthenticationPrincipal User user,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "p.createdAt"));
        boolean isSelf = userId == null || userId.getId().equals(user.getId());
        Long queryId = userId == null ? user.getId() : userId.getId();
        Long selfId = user.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子列表成功", new PageResult<>(postService.getPostsByUser(isSelf, queryId, selfId, pageable))));
    }

    @GetMapping("comments")
    public ApiResponseEntity<PageResult<CommentSummary>> getComments(
            @AuthenticationPrincipal User user,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
        boolean isSelf = userId == null || userId.getId().equals(user.getId());
        Long queryId = userId == null ? user.getId() : userId.getId();
        Long selfId = user.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取评论列表成功", new PageResult<>(commentService.getCommentsByUser(queryId, selfId, pageable))));
    }

    @GetMapping("comment_opinions")
    public ApiResponseEntity<PageResult<UserOpinionCommentSummary>> getCommentOpinions(
            @AuthenticationPrincipal User user,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
        Long queryId = userId == null? user.getId() : userId.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取评论态度列表成功", new PageResult<>(commentService.getCommentOpinionsByUser(queryId, opinion, pageable))));
    }

    @GetMapping("post_opinions")
    public ApiResponseEntity<PageResult<UserOpinionPostSummary>> getPostOpinions(
    @AuthenticationPrincipal User user,
    @RequestParam int page,
    @RequestParam(required = false) UserId userId,
    @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
    @RequestParam(defaultValue = "50") int size,
    @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
        Long queryId = userId == null? user.getId() : userId.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取帖子态度列表成功", new PageResult<>(postService.getPostOpinionsByUser(queryId, opinion, pageable))));
    }

    @GetMapping("avatars")
    public ApiResponseEntity<List<Avatar>> getUserAvatar(@RequestParam List<String> names) {
        var ret = userService.getUserAvatars(names);
        return ApiResponseEntity.ok(ret, "success");
    }
}
