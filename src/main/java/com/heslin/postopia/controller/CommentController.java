package com.heslin.postopia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.service.comment.CommentService;

@RestController("comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    public record CommentReplyDto(String content, Long commentId, Long postId) {}

    @PostMapping("reply")
    public BasicApiResponseEntity reply(@RequestBody CommentReplyDto dto) {
        if (dto.commentId() == null || dto.postId() == null || dto.content() == null)
            throw new BadRequestException("parameters content, commentId and postId are required");
        Post post = new Post();
        post.setId(dto.postId());
        Comment comment = new Comment();
        comment.setId(dto.commentId());
        commentService.reply(post, comment, dto.content(), null);
        return BasicApiResponseEntity.ok("回复成功");
    }

    public record CommentDeleteDto(Long id) {}
    @PostMapping("delete")
    public BasicApiResponseEntity delete(@RequestBody CommentDeleteDto dto) {
        commentService.checkAuthority(dto.id(), null);
        commentService.deleteComment(dto.id());
        return BasicApiResponseEntity.ok("删除成功");
    }   
}
