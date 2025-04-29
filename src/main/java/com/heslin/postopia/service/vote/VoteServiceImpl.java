package com.heslin.postopia.service.vote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.dto.VoteInfo;
import com.heslin.postopia.enums.DetailVoteType;
import com.heslin.postopia.enums.VoteType;
import com.heslin.postopia.enums.kafka.VoteOperation;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.model.Vote;
import com.heslin.postopia.jpa.model.opinion.VoteOpinion;
import com.heslin.postopia.jpa.repository.VoteRepository;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.schedule.ScheduleService;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.service.opinion.OpinionService;
import com.heslin.postopia.service.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final OpinionService opinionService;
    private final KafkaService kafkaService;
    private final ScheduleService scheduleService;
    private final CommentService commentService;
    private final PostService postService;
    private final ObjectMapper objectMapper;

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
    public VoteServiceImpl(VoteRepository voteRepository, OpinionService opinionService, KafkaService kafkaService, ScheduleService scheduleService, CommentService commentService, PostService postService, ObjectMapper objectMapper) {
        this.voteRepository = voteRepository;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
        this.scheduleService = scheduleService;
        this.commentService = commentService;
        this.postService = postService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void upsertVoteOpinion(User user, Long id, boolean isPositive) {
        VoteOpinion voteOpinion = new VoteOpinion();
        voteOpinion.setUser(user);
        voteOpinion.setUsername(user.getUsername());
        voteOpinion.setVote(Vote.builder().id(id).build());
        voteOpinion.setPositive(isPositive);
        boolean isInsert = opinionService.upsertOpinion(voteOpinion);
        if (isInsert) {
            kafkaService.sendToVote(id, isPositive? VoteOperation.AGREED : VoteOperation.DISAGREED);
        } else {
            kafkaService.sendToVote(id, isPositive? VoteOperation.SWITCH_TO_AGREE : VoteOperation.SWITCH_TO_DISAGREE);
        }
    }

    private List<VoteInfo> getVotes(List<Long> ids, VoteType voteType) {
        return voteRepository.findVotes(ids, voteType);
    }

    @Override
    public List<VoteInfo> getCommentVotes(List<Long> commentIds) {
        return getVotes(commentIds, VoteType.COMMENT);
    }

    @Override
    public List<VoteInfo> getPostVotes(List<Long> ids) {
        return getVotes(ids, VoteType.POST);
    }

    @Override
    public VoteInfo getSpaceVote(Long id) {
        return voteRepository.findSpaceVote(id);
    }

    private Vote createVote(User user, Long relatedId, String additional, VoteType voteType, DetailVoteType detailVoteType) {
        return createVote(user, relatedId, additional, voteType, detailVoteType, null);
    }

    private Vote createVote(User user, Long relatedId, String additional, VoteType voteType, DetailVoteType detailVoteType, Long spaceMember) {
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
            } case SPACE -> {
                if (spaceMember < 100) {
                    threshold = (long) (spaceMember * spaceSmall);
                } else if (spaceMember < 1000) {
                    threshold = (long) (spaceMember * spaceMedium);
                } else {
                    threshold = (long) (spaceMember * spaceLarge);
                }
                end = start.plus(spaceDuration, ChronoUnit.MINUTES);
            }default -> throw new IllegalStateException("Unexpected value: " + voteType);
        }
        Vote vote = Vote.builder()
        .initiator(user.getUsername())
        .relatedId(relatedId)
        .additional(additional)
        .startAt(start)
        .endAt(end)
        .voteType(voteType)
        .detailVoteType(detailVoteType)
        .positiveCount(1)
        .negativeCount(0)
        .threshold(threshold)
        .build();
        return voteRepository.save(vote);
    }

    record UpdateSpaceInfo(String avatar, String description) {}

    record SpaceUserOpInfo(String username, String reason) {}

    @Override
    public Long updateSpaceVote(User user, Long id, String name, Long member, String avatar, String description) {
        String info;
        try {
            info = objectMapper.writeValueAsString(new UpdateSpaceInfo(avatar, description));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Vote vote = createVote(user, id, info, VoteType.SPACE, DetailVoteType.UPDATE_SPACE, member);
        scheduleService.scheduleUpdateSpaceVote(vote.getId(), name, description, avatar, vote.getEndAt());
        return vote.getId();
    }

    @Override
    public Long expelSpaceUserVote(User user, Long spaceId, String spaceName, Long member, String username, String reason) {
        String info;
        try {
            info = objectMapper.writeValueAsString(new SpaceUserOpInfo(username, reason));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Vote vote = createVote(user, spaceId, info, VoteType.SPACE, DetailVoteType.EXPEL_USER, member);
        scheduleService.scheduleExpelSpaceUserVote(vote.getId(), spaceName, username, reason, vote.getEndAt());
        return vote.getId();
    }

    @Override
    public Long muteSpaceUserVote(User user, Long spaceId, String spaceName, Long member, String username, String reason) {
        String info;
        try {
            info = objectMapper.writeValueAsString(new SpaceUserOpInfo(username, reason));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Vote vote = createVote(user, spaceId, info, VoteType.SPACE, DetailVoteType.MUTE_USER, member);
        scheduleService.scheduleMuteSpaceUserVote(vote.getId(), spaceName, username, reason, vote.getEndAt());
        return vote.getId();
    }

    @Override
    public Long deletePostVote(User user, Long postId, String postSubject, String postAuthor, String spaceName) {
        Vote vote = createVote(user, postId, postAuthor, VoteType.POST, DetailVoteType.DELETE_POST);
        scheduleService.scheduleDeletePostVote(vote.getId(), spaceName, postSubject, vote.getEndAt());
        return vote.getId();
    }

    private Long updateArchiveStatusVote(boolean isArchived, User user, Long postId, String spaceName, String postSubject, String postAuthor) throws BadRequestException {
        if (!postService.checkPostArchiveStatus(postId, isArchived)) {
            throw new BadRequestException(isArchived? "该评论归档" : "该评论未归档");
        }
        Vote vote = createVote(user, postId, postAuthor, VoteType.POST, isArchived? DetailVoteType.ARCHIVE_POST : DetailVoteType.UNARCHIVE_POST);
        scheduleService.scheduleUpdatePostArchiveStatusVote(vote.getId(), isArchived, postId, spaceName, postSubject, postAuthor, vote.getEndAt());
        return vote.getId();
    }

    @Override
    public Long unArchivePostVote(User user, Long postId, String postSubject, String postAuthor, String spaceName) throws BadRequestException {
        return updateArchiveStatusVote(false, user, postId, spaceName, postSubject, postAuthor);
    }

    @Override
    public Long archivePostVote(User user, Long postId, String postSubject, String postAuthor, String spaceName) throws BadRequestException {
        return updateArchiveStatusVote(true, user, postId, spaceName, postSubject, postAuthor);
    }

    @Override
    public Long deleteCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) {
        Vote vote = createVote(user, commentId, commentAuthor, VoteType.COMMENT, DetailVoteType.DELETE_COMMENT);
        scheduleService.scheduleDeleteCommentVote(vote.getId(), postId, spaceName, commentContent, vote.getEndAt());
        return vote.getId();
    }

    private Long updatePinStatusVote(boolean isPined, User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException {
        if (!commentService.checkCommentPinStatus(commentId, isPined)) {
            throw new BadRequestException(isPined? "该评论已置顶" : "该评论未置顶");
        }
        Vote vote = createVote(user, commentId, commentAuthor, VoteType.COMMENT, isPined? DetailVoteType.PIN_COMMENT : DetailVoteType.UNPIN_COMMENT);
        scheduleService.scheduleUpdateCommentPinStatusVote(vote.getId(), isPined, commentId, postId, spaceName, commentContent, vote.getEndAt());
        return vote.getId();
    }

    @Override
    public Long pinCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException {
        return updatePinStatusVote(true, user, commentId, postId, spaceName, commentContent, commentAuthor);
    }

    @Override
    public Long unPinCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException {
        return updatePinStatusVote(false, user, commentId, postId, spaceName, commentContent, commentAuthor);
    }
}
