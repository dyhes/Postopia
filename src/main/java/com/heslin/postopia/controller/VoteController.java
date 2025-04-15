package com.heslin.postopia.controller;

import com.heslin.postopia.dto.comment.CommentVote;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.vote.VoteService;
import com.heslin.postopia.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vote")
public class VoteController {
    private final VoteService voteService;

    @Autowired
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping("comment")
    public ApiResponseEntity<List<CommentVote>> getCommentVotes(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(voteService.getCommentVotes(ids), "success");
    }

    public record OpinionRequest(
        Long id,
        boolean isPositive
    ){}

    @PostMapping("opinion")
    public BasicApiResponseEntity createOpinion(@RequestBody OpinionRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        voteService.upsertVoteOpinion(user, request.id, request.isPositive);
        return BasicApiResponseEntity.ok("success");
    }

    public record DeleteCommentVoteRequest(
        Long commentId,
        String commentContent,
        String commentAuthor,
        Long postId,
        String spaceName
    ){}

    @PostMapping("comment-delete")
    public ApiResponseEntity<Long> deleteCommentVote(@RequestBody DeleteCommentVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.deleteCommentVote(user, request.commentId, request.postId, request.spaceName, request.commentContent, request.commentAuthor);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该评论存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }
}
