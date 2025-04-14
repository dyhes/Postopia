package com.heslin.postopia.controller;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.PageResult;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
import com.heslin.postopia.jpa.model.Comment;
import com.heslin.postopia.jpa.model.Post;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public record CommentReplyDto(String content, Long commentId, Long postId, String spaceName, String replyUser) {}

    @PostMapping("reply")
    public BasicApiResponseEntity reply(@RequestBody CommentReplyDto dto, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(dto);
        Post post = Post.builder().id(dto.postId()).build();
        Comment comment = new Comment();
        comment.setId(dto.commentId());
        Space space = Space.builder().name(dto.spaceName).build();
        commentService.reply(post, comment, dto.content(), user, space, dto.replyUser());
        return BasicApiResponseEntity.ok("回复成功");
    }

    public record CommentOpinionDto(Long id, Long postId, String spaceName, boolean isPositive){}
    @PostMapping("opinion")
    public BasicApiResponseEntity upsertPostOpinion(@RequestBody CommentOpinionDto request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        commentService.upsertCommentOpinion(user, request.id, request.postId, request.spaceName, request.isPositive);
        return BasicApiResponseEntity.ok("success");
    }

    public record DeleteCommentOpinionDto(Long id, boolean isPositive){}
    @PostMapping("opinion-delete")
    public BasicApiResponseEntity deletePostOpinion(@RequestBody DeleteCommentOpinionDto request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        boolean success = commentService.deleteCommentOpinion(user, request.id, request.isPositive);
        return BasicApiResponseEntity.ok(success);
    }

    @GetMapping("list")
    public ApiResponseEntity<PageResult<CommentInfo>> getComments(@RequestParam(name = "postId") Long postId,
                                                                  @RequestParam int page,
                                                                  @RequestParam(defaultValue = "50") int size,
                                                                  @RequestParam(defaultValue = "ASC") Sort.Direction direction,
                                                                  @AuthenticationPrincipal User user
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "c.createdAt"));
        return ApiResponseEntity.ok(new PageResult<>(commentService.getCommentsByPost(postId,user.getId(), pageable)), "success", true);
    }

    @GetMapping("search-info")
    public ApiResponseEntity<List<SearchedCommentInfo>> getCommentInfosInSearch(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(commentService.getCommentInfosInSearch(ids), "success");
    }
}
