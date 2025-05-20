package com.heslin.postopia.comment.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("comment")
public class CommentController {
//    @PostMapping("Pindelete")
//    void deleteComment(@RequestParam Long spaceId, @RequestParam Long postId, @RequestParam Long commentId, @RequestParam Long userId);
//
//    @GetMapping("Pinpin")
//    boolean checkPinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);
//
//    @PostMapping("Pinpin")
//    void updatePinStatus(@RequestParam Long commentId, @RequestParam boolean isPined);

    //    @GetMapping("comments")
//    public ApiResponseEntity<PageResult<CommentSummary>> getComments(
//            @RequestHeader Long userId,
//            @RequestParam int page,
//            @RequestParam(required = false) UserId userId,
//            @RequestParam(defaultValue = "50") int size,
//            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
//        boolean isSelf = userId == null || userId.getId().equals(user.getId());
//        Long queryId = userId == null ? user.getId() : userId.getId();
//        Long selfId = user.getId();
//        return ApiResponseEntity.ok(new ApiResponse<>("获取评论列表成功", new PageResult<>(commentService.getCommentsByUser(queryId, selfId, pageable))));
//    }
//
//    @GetMapping("comment_opinions")
//    public ApiResponseEntity<PageResult<UserOpinionCommentSummary>> getCommentOpinions(
//            @RequestHeader Long userId,
//            @RequestParam int page,
//            @RequestParam(required = false) UserId userId,
//            @RequestParam(defaultValue = "NIL") OpinionStatus opinion,
//            @RequestParam(defaultValue = "50") int size,
//            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "o.updatedAt"));
//        Long queryId = userId == null? user.getId() : userId.getId();
//        return ApiResponseEntity.ok(new ApiResponse<>("获取评论态度列表成功", new PageResult<>(commentService.getCommentOpinionsByUser(queryId, opinion, pageable))));
//    }

//    @PostMapping("reply")
//    public ApiResponseEntity<Long> replyPost(@AuthenticationPrincipal User user, @RequestBody ReplyPostDto request) {
//        PostopiaFormatter.isValidComment(request.content);
//        Utils.checkRequestBody(request);
//        postService.validate(user, request.spaceName);
//        Post post = Post.builder().id(request.postId).build();
//        Space space = Space.builder().name(request.spaceName).build();
//        //postService.checkPostStatus(request.id);
//        Comment comment = postService.replyPost(post, request.content, user, space, request.replyUserId.getId(), request.replyUser);
//        return BasicApiResponseEntity.ok(comment.getId(), "回复成功", true);
//    }
}
