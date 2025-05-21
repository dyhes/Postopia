package com.heslin.postopia.comment.controller;

import com.heslin.postopia.comment.dto.CommentInfo;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.dto.response.PageResult;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.model.Post;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comment")
public class CommentController {
    @PostMapping("Pindelete")
    void deleteComment(@RequestParam Long spaceId, @RequestParam Long postId, @RequestParam Long commentId, @RequestParam Long userId);

    @GetMapping("Pinpin")
    boolean checkPinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);

    @PostMapping("Pinpin")
    void updatePinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);

    @PostMapping("reply")
    public ApiResponseEntity<Long> replyPost(@AuthenticationPrincipal User user, @RequestBody ReplyPostDto request) {
        PostopiaFormatter.isValidComment(request.content);
        Utils.checkRequestBody(request);
        postService.validate(user, request.spaceName);
        Post post = Post.builder().id(request.postId).build();
        Space space = Space.builder().name(request.spaceName).build();
        //postService.checkPostStatus(request.id);
        Comment comment = postService.replyPost(post, request.content, user, space, request.replyUserId.getId(), request.replyUser);
        return BasicApiResponseEntity.ok(comment.getId(), "回复成功", true);
    }

    @PostMapping("reply")
    public BasicApiResponseEntity reply(@RequestBody CommentReplyDto dto, @AuthenticationPrincipal User user) {
        commentService.validate(user, dto.spaceName);
        PostopiaFormatter.isValidComment(dto.content);
        Utils.checkRequestBody(dto);
        Post post = Post.builder().id(dto.postId()).build();
        Comment comment = new Comment();
        comment.setId(dto.commentId());
        Space space = Space.builder().name(dto.spaceName).build();
        commentService.reply(post, comment, dto.content(), user, space, dto.replyUserId.getId() ,dto.replyUsername());
        return BasicApiResponseEntity.ok("回复成功");
    }

    @GetMapping("post")
    public ApiResponseEntity<PageResult<CommentInfo>> getComments(@RequestParam(name = "postId") Long postId,
                                                                  @RequestParam int page,
                                                                  @RequestParam(defaultValue = "50") int size,
                                                                  @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                                                  @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
        return ApiResponseEntity.ok(new PageResult<>(commentService.getCommentsByPost(postId,user.getId(), pageable)), "success", true);
    }

    @GetMapping("search")
    public ApiResponseEntity<List<SearchedCommentInfo>> getCommentInfosInSearch(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(commentService.getCommentInfosInSearch(ids), "success");
    }

    @GetMapping("user/opinion")
    public ApiResponseEntity<PageResult<UserOpinionCommentSummary>> getCommentOpinions(
            @RequestHeader Long userId,
            @RequestParam int page,
            @RequestParam(required = false) UserId userId,
            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
        Long queryId = userId == null? user.getId() : userId.getId();
        return ApiResponseEntity.ok(new ApiResponse<>("获取评论态度列表成功", new PageResult<>(commentService.getCommentOpinionsByUser(queryId, opinion, pageable))));
    }

    @GetMapping("user")
    public ApiResponseEntity<PageResult<CommentSummary>> getComments(
            @RequestHeader Long userId,
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

}
