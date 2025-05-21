package com.heslin.postopia.comment.controller;

import com.heslin.postopia.comment.dto.CommentInfo;
import com.heslin.postopia.comment.dto.OpinionCommentInfo;
import com.heslin.postopia.comment.dto.SearchCommentInfo;
import com.heslin.postopia.comment.dto.UserCommentInfo;
import com.heslin.postopia.comment.model.Comment;
import com.heslin.postopia.comment.request.CreateCommentRequest;
import com.heslin.postopia.comment.service.CommentService;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.*;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comment")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("create")
    public ApiResponseEntity<Long> createComment(@RequestHeader Long xUserId, @RequestParam CreateCommentRequest request) {
        Long id = commentService.createComment(xUserId, request);
        return ApiResponseEntity.success(id);
    }

    @PostMapping("delete")
    public void deleteComment(@RequestParam Long spaceId, @RequestParam Long postId, @RequestParam Long commentId, @RequestParam Long userId) {
        commentService.deleteComment(spaceId, postId, commentId, userId);
    }

    @GetMapping("pin")
    public boolean checkPinStatus(@RequestParam Long commentId, @RequestParam boolean isPined) {
        return commentService.checkPinStatus(commentId, isPined);
    }

    @PostMapping("pin")
    public void updatePinStatus(@RequestParam Long commentId, @RequestParam boolean isPined) {
        commentService.updatePinStatus(commentId, isPined);
    }

    @GetMapping("post")
    public PagedApiResponseEntity<CommentInfo> getComments(
        @RequestHeader Long xUserId,
        @RequestParam(name = "postId") Long postId,
        @RequestParam int page,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
        return ApiResponseEntity.ok(new PageResult<>(commentService.getCommentsByPost(postId,user.getId(), pageable)), "success", true);
    }

    @GetMapping("search")
    public ApiResponseEntity<List<SearchCommentInfo>> getCommentInfosInSearch(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(commentService.getCommentInfosInSearch(ids), "success");
    }

    @GetMapping("user")
    public PagedApiResponseEntity<UserCommentInfo> getComments(
        @RequestHeader Long xUserId,
        @RequestParam int page,
        @RequestParam(required = false) UserId userId,
        @RequestParam(defaultValue = "30") int size,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
        boolean isSelf = userId == null || userId.getId().equals(user.getId());
        Long queryId = userId == null ? user.getId() : userId.getId();
        Long selfId = user.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取评论列表成功", new PageResult<>(commentService.getCommentsByUser(queryId, selfId, pageable))));
    }

    @GetMapping("user/opinion")
    public PagedApiResponseEntity<OpinionCommentInfo> getCommentOpinions(
            @RequestHeader Long xUserId,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
        Long queryId = userId == null? user.getId() : userId.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取评论态度列表成功", new PageResult<>(commentService.getCommentOpinionsByUser(queryId, opinion, pageable))));
    }
}
