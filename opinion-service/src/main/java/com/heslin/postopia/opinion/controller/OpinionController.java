package com.heslin.postopia.opinion.controller;

import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.opinion.enums.OpinionType;
import com.heslin.postopia.opinion.request.OpinionRequest;
import com.heslin.postopia.opinion.request.UpsertCommentRequest;
import com.heslin.postopia.opinion.request.UpsertPostRequest;
import com.heslin.postopia.opinion.service.OpinionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("opinion")
public class OpinionController {
    private final OpinionService opinionService;

    @Autowired
    public OpinionController(OpinionService opinionService) {
        this.opinionService = opinionService;
    }

    @GetMapping("user/post")
    public Page<OpinionInfo> getUserPostOpinion(@RequestParam Long userId, @RequestParam int page, @RequestParam int size, @RequestParam Sort.Direction direction, @RequestParam OpinionStatus status) {
        System.out.println("direction = " + direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "updatedAt"));
        return opinionService.getUserPostOpinion(userId, status, pageable);
    }

    @GetMapping("user/comment")
    public Page<OpinionInfo> getUserCommentOpinion(@RequestParam Long userId, @RequestParam int page, @RequestParam int size, @RequestParam Sort.Direction direction, @RequestParam OpinionStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "updatedAt"));
        return opinionService.getUserCommentOpinion(userId, status, pageable);
    }

    @GetMapping("post")
    public List<OpinionInfo> getPostOpinion(@RequestParam Long userId, @RequestParam List<Long> postId) {
        return opinionService.getOpinion(userId, postId, OpinionType.POST);
    }

    @GetMapping("comment")
    public List<OpinionInfo> getCommentOpinion(@RequestParam Long userId, @RequestParam List<Long> commentId) {
        return opinionService.getOpinion(userId, commentId, OpinionType.COMMENT);
    }

    @GetMapping("vote")
    public List<OpinionInfo> getVoteOpinion(@RequestParam Long userId, @RequestParam List<Long> voteId) {
        return opinionService.getOpinion(userId, voteId, OpinionType.VOTE);
    }

    @PostMapping("vote")
    public BasicApiResponseEntity createVoteOpinion(@RequestBody OpinionRequest request, @RequestHeader Long xUserId, @RequestParam boolean isCommon) {
        Utils.checkRequestBody(request);
        opinionService.upsertVoteOpinion(xUserId, request.isPositive(), request.id(), isCommon);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("/vote/notify")
    public void notifyVoter(@RequestParam Long voteId, @RequestParam String message) {
        opinionService.notifyVoter(voteId, message);
    }

    // 非幂等！！！
    @PostMapping("post")
    public BasicApiResponseEntity upsertPostOpinion(@RequestBody UpsertPostRequest request, @RequestHeader Long xUserId, @RequestHeader String xUsername) {
        Utils.checkRequestBody(request);
        opinionService.upsertPostOpinion(xUserId, xUsername, request);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("comment")
    public BasicApiResponseEntity upsertCommentOpinion(@RequestBody UpsertCommentRequest request, @RequestHeader String xUsername, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        opinionService.upsertCommentOpinion(xUserId, xUsername, request);
        return BasicApiResponseEntity.success();
    }

    @PostMapping("comment/delete")
    public BasicApiResponseEntity deleteCommentOpinion(@RequestBody OpinionRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        opinionService.deleteCommentOpinion(xUserId, request.id(), request.isPositive());
        return BasicApiResponseEntity.success();
    }

    @PostMapping("post/delete")
    public BasicApiResponseEntity deletePostOpinion(@RequestBody OpinionRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        opinionService.deletePostOpinion(xUserId, request.id(), request.isPositive());
        return BasicApiResponseEntity.success();
    }
}
