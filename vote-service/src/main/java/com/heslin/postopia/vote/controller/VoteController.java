package com.heslin.postopia.vote.controller;

import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.space.dto.VoteSpaceInfo;
import com.heslin.postopia.vote.dto.SpaceVoteInfo;
import com.heslin.postopia.vote.dto.VoteInfo;
import com.heslin.postopia.vote.request.CommentVoteRequest;
import com.heslin.postopia.vote.request.PostVoteRequest;
import com.heslin.postopia.vote.request.SpaceInfoRequest;
import com.heslin.postopia.vote.request.SpaceUserRequest;
import com.heslin.postopia.vote.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("vote")
public class VoteController {
    private final VoteService voteService;

    @Autowired
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping("comment")
    public CompletableFuture<List<VoteInfo>> getCommentVotes(@RequestParam Long userId, @RequestParam List<Long> ids) {
        return voteService.getCommentVotes(userId, ids);
    }

    @GetMapping("post")
    public CompletableFuture<List<VoteInfo>> getPostVotes(@RequestParam Long userId, @RequestParam List<Long> ids) {
        return voteService.getPostVotes(userId, ids);
    }

    @GetMapping("space")
    public CompletableFuture<ApiResponseEntity<List<SpaceVoteInfo>>> getSpaceVote(@RequestParam Long userId, @RequestParam Long spaceId) {
        return voteService.getSpaceVote(spaceId);
    }


    private VoteSpaceInfo spaceMemberCheck(Long spaceId, Long userId) {
        Pair<Boolean, VoteSpaceInfo> pair = voteService.spaceMemberCheck(spaceId, userId);
        if (!pair.getFirst()) {
            throw new RuntimeException("该用户不在该空间内");
        }
        return pair.getSecond();
    }

    @PostMapping("user-expel")
    public ApiResponseEntity<Long> expelSpaceUserVote(@RequestBody SpaceUserRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            VoteSpaceInfo voteSpaceInfo = spaceMemberCheck(request.spaceId(), xUserId);
            Long id = voteService.expelSpaceUserVote(xUserId, voteSpaceInfo, request);
            return ApiResponseEntity.success(id);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("user-mute")
    public ApiResponseEntity<Long> muteSpaceUserVote(@RequestBody SpaceUserRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            VoteSpaceInfo voteSpaceInfo = spaceMemberCheck(request.spaceId(), xUserId);
            Long id = voteService.muteSpaceUserVote(xUserId, voteSpaceInfo, request);
            return ApiResponseEntity.success(id);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }


    @PostMapping("space-update")
    public ApiResponseEntity<Long> updateSpaceVote(@RequestPart(required = false, name = "avatar") MultipartFile file, @RequestPart(name="info") SpaceInfoRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            String avatar = voteService.uploadAvatar(xUserId, file);
            VoteSpaceInfo voteSpaceInfo = spaceMemberCheck(request.spaceId(), xUserId);
            Long id = voteService.updateSpaceVote(xUserId, voteSpaceInfo, request.spaceId(), avatar == null? request.avatar(): avatar, request.description());
            return ApiResponseEntity.success(id);
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }



    @PostMapping("post-delete")
    public ApiResponseEntity<Long> deletePostVote(@RequestBody PostVoteRequest request, @RequestHeader Long xUserId) {
        try {
            Long id = voteService.deletePostVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该帖子存在正在进行的投票");
        }

    }

    @PostMapping("post-archive")
    public ApiResponseEntity<Long> archivePostVote(@RequestBody PostVoteRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            Long id = voteService.archivePostVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该帖子存在正在进行的投票");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("post-unarchive")
    public ApiResponseEntity<Long> unArchivePostVote(@RequestBody PostVoteRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            Long id = voteService.unArchivePostVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该帖子存在正在进行的投票");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }


    @PostMapping("comment-delete")
    public ApiResponseEntity<Long> deleteCommentVote(@RequestBody CommentVoteRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            Long id = voteService.deleteCommentVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该评论存在正在进行的投票");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("comment-pin")
    public ApiResponseEntity<Long> pinCommentVote(@RequestBody CommentVoteRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            Long id = voteService.pinCommentVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该评论存在正在进行的投票");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }

    @PostMapping("comment-unpin")
    public ApiResponseEntity<Long> unPinCommentVote(@RequestBody CommentVoteRequest request, @RequestHeader Long xUserId) {
        Utils.checkRequestBody(request);
        try {
            Long id = voteService.unPinCommentVote(xUserId, request);
            return ApiResponseEntity.success(id);
        } catch (DataIntegrityViolationException e) {
            return ApiResponseEntity.fail("该评论存在正在进行的投票");
        } catch (RuntimeException e) {
            return ApiResponseEntity.fail(e.getMessage());
        }
    }
}
