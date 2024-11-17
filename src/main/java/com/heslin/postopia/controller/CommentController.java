package com.heslin.postopia.controller;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import com.heslin.postopia.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    public record CommentReplyDto(String content, Long commentId, Long postId) {}

    @PostMapping("reply")
    public BasicApiResponseEntity reply(@RequestBody CommentReplyDto dto, @AuthenticationPrincipal User user) {
        if (dto.commentId() == null || dto.postId() == null || dto.content() == null)
            throw new BadRequestException("parameters content, commentId and postId are required");
        Post post = new Post();
        post.setId(dto.postId());
        Comment comment = new Comment();
        comment.setId(dto.commentId());
        commentService.reply(post, comment, dto.content(), user);
        return BasicApiResponseEntity.ok("回复成功");
    }

    public record CommentIdDto(Long id) {
    }

    ;

    @PostMapping("delete")
    public BasicApiResponseEntity delete(@RequestBody CommentIdDto dto, @AuthenticationPrincipal User user) {
        commentService.checkAuthority(dto.id(), user);
        commentService.deleteComment(dto.id());
        return BasicApiResponseEntity.ok("删除成功");
    }

    @PostMapping("like")
    public BasicApiResponseEntity likeComment(@RequestBody CommentIdDto dto) {
        if (dto.id == null) {
            throw new BadRequestException("commentId is required");
        }
        commentService.likeComment(dto.id);
        return BasicApiResponseEntity.ok("comment liked!");
    }

    @PostMapping("dislike")
    public BasicApiResponseEntity disLikeComment(@RequestBody CommentIdDto dto) {
        if (dto.id == null) {
            throw new BadRequestException("commentId is required");
        }
        commentService.disLikeComment(dto.id);
        return BasicApiResponseEntity.ok("comment disliked!");
    }

    @GetMapping("list")
    public ApiResponseEntity<List<CommentInfo>> getComments(@RequestParam(name = "postId") Long postId) {
        return ApiResponseEntity.ok(commentService.getCommentsByPost(postId), "success", true);
    }
}
