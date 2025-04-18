package com.heslin.postopia.controller;

import com.heslin.postopia.dto.VoteInfo;
import com.heslin.postopia.dto.response.ApiResponseEntity;
import com.heslin.postopia.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.exception.BadRequestException;
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
    public ApiResponseEntity<List<VoteInfo>> getCommentVotes(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(voteService.getCommentVotes(ids), "success");
    }

    @GetMapping("post")
    public ApiResponseEntity<List<VoteInfo>> getPostVotes(@RequestParam List<Long> ids) {
        return ApiResponseEntity.ok(voteService.getPostVotes(ids), "success");
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

    public record PostVoteRequest(
        Long postId,
        String postSubject,
        String postAuthor,
        String spaceName
    ){}

    @PostMapping("post-delete")
    public ApiResponseEntity<Long> deletePostVote(@RequestBody PostVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.deletePostVote(user, request.postId, request.postSubject, request.postAuthor, request.spaceName);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该帖子存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }

    @PostMapping("post-archive")
    public ApiResponseEntity<Long> archivePostVote(@RequestBody PostVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.archivePostVote(user, request.postId, request.postSubject, request.postAuthor, request.spaceName);
        }  catch (BadRequestException e) {
            return ApiResponseEntity.ok(null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该帖子存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }

    @PostMapping("post-unarchive")
    public ApiResponseEntity<Long> unArchivePostVote(@RequestBody PostVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.unArchivePostVote(user, request.postId, request.postSubject, request.postAuthor, request.spaceName);
        }  catch (BadRequestException e) {
            return ApiResponseEntity.ok(null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该帖子存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }

    public record CommentVoteRequest(
        Long commentId,
        String commentContent,
        String commentAuthor,
        Long postId,
        String spaceName
    ){}

    @PostMapping("comment-delete")
    public ApiResponseEntity<Long> deleteCommentVote(@RequestBody CommentVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.deleteCommentVote(user, request.commentId, request.postId, request.spaceName, request.commentContent, request.commentAuthor);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该评论存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }

    @PostMapping("comment-pin")
    public ApiResponseEntity<Long> pinCommentVote(@RequestBody CommentVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.pinCommentVote(user, request.commentId, request.postId, request.spaceName, request.commentContent, request.commentAuthor);
        } catch (BadRequestException e) {
            return ApiResponseEntity.ok(null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该评论存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }

    @PostMapping("comment-unpin")
    public ApiResponseEntity<Long> unPinCommentVote(@RequestBody CommentVoteRequest request, @AuthenticationPrincipal User user) {
        Utils.checkRequestBody(request);
        Long id;
        try {
            id = voteService.unPinCommentVote(user, request.commentId, request.postId, request.spaceName, request.commentContent, request.commentAuthor);
        } catch (BadRequestException e) {
            return ApiResponseEntity.ok(null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.ok(null, "该评论存在正在进行的投票");
        }
        return ApiResponseEntity.ok(id, "success");
    }
}
