package com.heslin.postopia.controller;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.PageResult;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
import com.heslin.postopia.exception.BadRequestException;
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

    public record CommentReplyDto(String content, Long commentId, Long postId, String spaceName) {}

    @PostMapping("reply")
    public BasicApiResponseEntity reply(@RequestBody CommentReplyDto dto, @AuthenticationPrincipal User user) {
        if (Utils.allFieldsNonNull(dto)) {
            throw new BadRequestException("parameters are required");
        }
        Post post = Post.builder().id(dto.postId()).build();
        Comment comment = new Comment();
        comment.setId(dto.commentId());
        Space space = Space.builder().name(dto.spaceName).build();
        commentService.reply(post, comment, dto.content(), user, space);
        return BasicApiResponseEntity.ok("回复成功");
    }

    public record CommentIdDto(Long id) {
    };

    public record DeleteDto(Long id, Long postId, String spaceName) {
    }

    @PostMapping("delete")
    public BasicApiResponseEntity delete(@RequestBody DeleteDto dto, @AuthenticationPrincipal User user) {
        if (dto.id == null || dto.postId == null) {
            throw new BadRequestException("id and postId cannot be null");
        }
        boolean success = commentService.deleteComment(dto.id(), dto.postId(), user.getId(), dto.spaceName);
        return BasicApiResponseEntity.ok(success);
    }

    @PostMapping("like")
    public BasicApiResponseEntity likeComment(@RequestBody CommentIdDto dto, @AuthenticationPrincipal User user) {
        if (dto.id == null) {
            throw new BadRequestException("commentId is required");
        }
        commentService.likeComment(dto.id, user);
        return BasicApiResponseEntity.ok("comment liked!");
    }

    @PostMapping("dislike")
    public BasicApiResponseEntity disLikeComment(@RequestBody CommentIdDto dto, @AuthenticationPrincipal User user) {
        if (dto.id == null) {
            throw new BadRequestException("commentId is required");
        }
        commentService.disLikeComment(dto.id, user);
        return BasicApiResponseEntity.ok("comment disliked!");
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
