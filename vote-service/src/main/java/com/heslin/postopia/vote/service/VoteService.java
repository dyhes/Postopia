package com.heslin.postopia.vote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.response.ApiResponseEntity;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.space.dto.VoteSpaceInfo;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.vote.dto.SpaceVoteInfo;
import com.heslin.postopia.vote.dto.SpaceVotePart;
import com.heslin.postopia.vote.dto.VoteInfo;
import com.heslin.postopia.vote.dto.VotePart;
import com.heslin.postopia.vote.enums.DetailVoteType;
import com.heslin.postopia.vote.enums.VoteType;
import com.heslin.postopia.vote.feign.*;
import com.heslin.postopia.vote.model.CommonVote;
import com.heslin.postopia.vote.model.SpaceVote;
import com.heslin.postopia.vote.model.Vote;
import com.heslin.postopia.vote.repository.CommonVoteRepository;
import com.heslin.postopia.vote.repository.SpaceVoteRepository;
import com.heslin.postopia.vote.request.CommentVoteRequest;
import com.heslin.postopia.vote.request.PostVoteRequest;
import com.heslin.postopia.vote.request.SpaceUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@RefreshScope
@Service
public class VoteService {
    private final CommonVoteRepository commonVoteRepository;
    private final SpaceVoteRepository spaceVoteRepository;
    private final KafkaService kafkaService;
    private final VoteScheduleService scheduleService;
    private final ObjectMapper objectMapper;
    private final UserFeign userFeign;
    private final SpaceFeign spaceFeign;
    private final PostFeign postFeign;
    private final CommentFeign commentFeign;
    private final OpinionFeign opinionFeign;
//    private final OpinionService opinionService;
//    private final CommentService commentService;
//    private final PostService postService;

    @Value("${postopia.vote.comment.duration}")
    private Long commentDuration;
    @Value("${postopia.vote.comment.threshold}")
    private Long commentThreshold;
    @Value("${postopia.vote.post.duration}")
    private Long postDuration;
    @Value("${postopia.vote.post.threshold}")
    private Long postThreshold;
    @Value("${postopia.vote.space.duration}")
    private Long spaceDuration;
    @Value("${postopia.vote.space.large}")
    private float spaceLarge;
    @Value("${postopia.vote.space.medium}")
    private float spaceMedium;
    @Value("${postopia.vote.space.small}")
    private float spaceSmall;

    @Autowired
    public VoteService(CommonVoteRepository commonVoteRepository, SpaceVoteRepository spaceVoteRepository, KafkaService kafkaService, VoteScheduleService scheduleService, ObjectMapper objectMapper, UserFeign userFeign, SpaceFeign spaceFeign, PostFeign postFeign, CommentFeign commentFeign, OpinionFeign opinionFeign) {
        this.commonVoteRepository = commonVoteRepository;
        this.spaceVoteRepository = spaceVoteRepository;
        this.kafkaService = kafkaService;
        this.scheduleService = scheduleService;
        this.objectMapper = objectMapper;
        this.userFeign = userFeign;
        this.spaceFeign = spaceFeign;
        this.postFeign = postFeign;
        this.commentFeign = commentFeign;
        this.opinionFeign = opinionFeign;
    }

    public Pair<Boolean, VoteSpaceInfo> spaceMemberCheck(Long spaceId, Long userId) {
        return spaceFeign.checkMemberForVote(spaceId, userId);
    }

    public CompletableFuture<List<VoteInfo>> asyncCompleteVote(Long userId, List<Long> ids, List<VotePart> voteParts) {
        if (voteParts == null || voteParts.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        List<Long> voteIds = new ArrayList<>();
        List<Long> initiator = new ArrayList<>();
        voteParts
        .forEach(votePart -> {
            voteIds.add(votePart.id());
            initiator.add(votePart.initiator());
        });
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(userId, voteIds);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(initiator);
        return CompletableFuture.allOf(futureUserInfo, futureOpinionInfo).thenApply(v -> {
            List<OpinionInfo> opinionInfos = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<VoteInfo> partial = Utils.triMerge(voteParts,
            opinionInfos, OpinionInfo::mergeId, (votePart, mp) -> mp.get(votePart.id()),
            userInfos, UserInfo::userId, (votePart, mp) -> mp.get(votePart.initiator()),
            (votePart, opinionInfo, userInfo) -> new VoteInfo(votePart.relatedEntity(), votePart, userInfo, opinionInfo)
            );
            Map<Long, VoteInfo> voteInfoMap = partial.stream().collect(Collectors.toMap(VoteInfo::mergeId, Function.identity()));
            return ids.stream().map(id -> voteInfoMap.getOrDefault(id, new VoteInfo(id))).toList();
        });
    }

    public CompletableFuture<List<VoteInfo>> getCommentVotes(Long userId, List<Long> ids) {
        List<VotePart> voteParts = commonVoteRepository.findVotes(ids, VoteType.COMMENT);
        return asyncCompleteVote(userId, ids, voteParts);
    }

    
    public CompletableFuture<List<VoteInfo>> getPostVotes(Long userId, List<Long> ids) {
        List<VotePart> voteParts = commonVoteRepository.findVotes(ids, VoteType.POST);
        return asyncCompleteVote(userId, ids, voteParts);
    }

    
    public CompletableFuture<ApiResponseEntity<List<SpaceVoteInfo>>> getSpaceVote(Long userId, Long id) {
        List<SpaceVotePart> spaceVoteParts = spaceVoteRepository.findByRelatedEntity(id);
        if (spaceVoteParts == null || spaceVoteParts.isEmpty()) {
            return CompletableFuture.completedFuture(ApiResponseEntity.success(Collections.emptyList()));
        }
        List<Long> voteIds = new ArrayList<>();
        List<Long> initiator = new ArrayList<>();
        List<Long> relatedUsers = new ArrayList<>();
        spaceVoteParts
        .forEach(votePart -> {
            voteIds.add(votePart.id());
            initiator.add(votePart.initiator());
            relatedUsers.add(votePart.relatedEntity());
        });
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(userId, voteIds);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(initiator);
        CompletableFuture<List<UserInfo>> futureRelatedUserInfo = userFeign.getUserInfos(relatedUsers);
        return CompletableFuture.allOf(futureUserInfo, futureOpinionInfo, futureRelatedUserInfo).thenApply(v -> {
            List<OpinionInfo> opinionInfos = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<UserInfo> relatedUserInfos = futureRelatedUserInfo.join();
            List<SpaceVoteInfo> res = Utils.quaMerge(spaceVoteParts,
            opinionInfos, OpinionInfo::mergeId, (votePart, mp) -> mp.get(votePart.id()),
            userInfos, UserInfo::userId, (votePart, mp) -> mp.get(votePart.initiator()),
            relatedUserInfos, UserInfo::userId, (votePart, mp) -> mp.get(votePart.relatedUser()),
            (votePart, opinionInfo, userInfo, relatedUserInfo) -> new SpaceVoteInfo(votePart, userInfo, opinionInfo, relatedUserInfo));
            return ApiResponseEntity.success(res);
        });
    }

    private SpaceVote createSpaceVote(Long xUserId, Long relatedEntity, Long relatedUser, DetailVoteType detailVoteType, Long spaceMember, String first, String second) {
        Instant start = Instant.now();
        Long threshold;
        Instant end = start.plus(spaceDuration, ChronoUnit.MINUTES);
        if (spaceMember < 100) {
            threshold = (long) (spaceMember * spaceSmall);
        } else if (spaceMember < 1000) {
            threshold = (long) (spaceMember * spaceMedium);
        } else {
            threshold = (long) (spaceMember * spaceLarge);
        }
        SpaceVote vote = SpaceVote.builder()
        .initiator(xUserId)
        .relatedEntity(relatedEntity)
        .relatedUser(relatedUser)
        .startAt(start)
        .endAt(end)
        .detailVoteType(detailVoteType)
        .positiveCount(1)
        .negativeCount(0)
        .threshold(threshold)
        .first(first)
        .second(second)
        .build();
        return spaceVoteRepository.save(vote);
    }

    public Long expelSpaceUserVote(Long xUserId, VoteSpaceInfo voteSpaceInfo, SpaceUserRequest request) {
        SpaceVote vote = createSpaceVote(xUserId, request.spaceId(), request.userId(), DetailVoteType.EXPEL_USER, voteSpaceInfo.memberCount(), request.username(), request.reason());
        String spaceMessage = PostopiaFormatter.formatSpace(request.spaceId(), voteSpaceInfo.name());
        scheduleService.scheduleExpelSpaceUserVote(vote.getId(), spaceMessage, request.userId(), request.username(), request.reason(), vote.getEndAt());
        return vote.getId();
    }

    public Long muteSpaceUserVote(Long xUserId, VoteSpaceInfo voteSpaceInfo, SpaceUserRequest request) {
        SpaceVote vote = createSpaceVote(xUserId, request.spaceId(), request.userId(), DetailVoteType.MUTE_USER, voteSpaceInfo.memberCount(), request.username(), request.reason());
        String spaceMessage = PostopiaFormatter.formatSpace(request.spaceId(), voteSpaceInfo.name());
        scheduleService.scheduleMuteSpaceUserVote(vote.getId(), spaceMessage, request.userId(), request.username(), request.reason(), vote.getEndAt());
        return vote.getId();
    }

    public Long updateSpaceVote(Long xUserId, VoteSpaceInfo voteSpaceInfo, Long spaceId, String avatar, String description) {
        SpaceVote vote = createSpaceVote(xUserId, spaceId, null, DetailVoteType.UPDATE_SPACE, voteSpaceInfo.memberCount(), description, avatar);
        String spaceMessage = PostopiaFormatter.formatSpace(spaceId, voteSpaceInfo.name());
        scheduleService.scheduleUpdateSpaceVote(vote.getId(), spaceMessage, description, avatar, vote.getEndAt());
        return vote.getId();
    }

    private CommonVote createCommonVote(Long xUserId, Long relatedEntity, Long relatedUser, VoteType voteType, DetailVoteType detailVoteType) {
        Instant start = Instant.now();
        Long threshold;
        Instant end;
        switch (voteType) {
            case COMMENT -> {
                threshold = commentThreshold;
                end = start.plus(commentDuration, ChronoUnit.MINUTES);
            }
            case POST -> {
                threshold = postThreshold;
                end = start.plus(postDuration, ChronoUnit.MINUTES);
            }
            default -> throw new IllegalStateException("Unexpected value: " + voteType);
        }
        CommonVote vote = CommonVote.builder()
        .initiator(xUserId)
        .relatedEntity(relatedEntity)
        .relatedUser(relatedUser)
        .startAt(start)
        .endAt(end)
        .detailVoteType(detailVoteType)
        .positiveCount(1)
        .negativeCount(0)
        .threshold(threshold)
        .voteType(voteType)
        .build();
        return commonVoteRepository.save(vote);
    }
    
    public Long deletePostVote(Long xUserId, PostVoteRequest request) {
        Vote vote = createCommonVote(xUserId, request.postId(), request.userId(), VoteType.POST, DetailVoteType.DELETE_POST);
        scheduleService.scheduleDeletePostVote(vote.getId(), request, vote.getEndAt());
        return vote.getId();
    }

    private Long updateArchiveStatusVote(boolean isArchived, Long xUserId, PostVoteRequest request) {
        if (!postFeign.checkPostArchiveStatus(request.postId(), isArchived)) {
            throw new RuntimeException(isArchived? "该评论归档" : "该评论未归档");
        }
        Vote vote = createCommonVote(xUserId, request.postId(), request.userId(), VoteType.POST, isArchived? DetailVoteType.ARCHIVE_POST : DetailVoteType.UNARCHIVE_POST);
        scheduleService.scheduleUpdatePostArchiveStatusVote(vote.getId(), isArchived, request, vote.getEndAt());
        return vote.getId();
    }

    
    public Long unArchivePostVote(Long xUserId, PostVoteRequest request) {
        return updateArchiveStatusVote(false, xUserId, request);
    }

    
    public Long archivePostVote(Long xUserId, PostVoteRequest request) {
        return updateArchiveStatusVote(true, xUserId, request);
    }

    
    public Long deleteCommentVote(Long xUserId, CommentVoteRequest request) {
        Vote vote = createCommonVote(xUserId, request.commentId(), request.userId(), VoteType.COMMENT, DetailVoteType.DELETE_COMMENT);
        scheduleService.scheduleDeleteCommentVote(vote.getId(), request, vote.getEndAt());
        return vote.getId();
    }

    private Long updatePinStatusVote(boolean isPined, Long xUserId, CommentVoteRequest request) {
        if (!commentFeign.checkPinStatus(request.commentId(), isPined)) {
            throw new RuntimeException(isPined? "该评论已置顶" : "该评论未置顶");
        }
        Vote vote = createCommonVote(xUserId, request.commentId(), request.userId(), VoteType.COMMENT, isPined? DetailVoteType.PIN_COMMENT : DetailVoteType.UNPIN_COMMENT);
        scheduleService.scheduleUpdateCommentPinStatusVote(vote.getId(), isPined, request, vote.getEndAt());
        return vote.getId();
    }

    
    public Long pinCommentVote(Long xUserId, CommentVoteRequest request) {
        return updatePinStatusVote(true, xUserId, request);
    }

    
    public Long unPinCommentVote(Long xUserId, CommentVoteRequest request) {
        return updatePinStatusVote(false, xUserId, request);
    }

    public String uploadAvatar(Long xUserId, MultipartFile file) {
        if (file != null) {
            var response = userFeign.uploadAvatar(file, false, xUserId);
            if (response.getStatusCode().is2xxSuccessful() && Objects.requireNonNull(response.getBody()).isSuccess()) {
                return Objects.requireNonNull(response.getBody()).getData();
            } else {
                throw new RuntimeException("上传图片失败");
            }
        }
        return null;
    }
}
