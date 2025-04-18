package com.heslin.postopia.service.vote;

import com.heslin.postopia.dto.comment.CommentVote;
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

    @Value("${postopia.vote.comment.duration}")
    private Long commentDuration;
    @Value("${postopia.vote.comment.threshold}")
    private Long commentThreshold;

    @Autowired
    public VoteServiceImpl(VoteRepository voteRepository, OpinionService opinionService, KafkaService kafkaService, ScheduleService scheduleService, CommentService commentService) {
        this.voteRepository = voteRepository;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
        this.scheduleService = scheduleService;
        this.commentService = commentService;
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

    @Override
    public List<CommentVote> getCommentVotes(List<Long> commentIds) {
        return voteRepository.findCommentVotes(commentIds);
    }

    private Vote createCommentVote(User user, Long commentId, String commentAuthor, DetailVoteType detailVoteType) {
        Instant start = Instant.now();
        Instant end = start.plus(commentDuration, ChronoUnit.MINUTES);
        Vote vote = Vote.builder()
        .initiator(user.getUsername())
        .relatedId(commentId)
        .relatedUser(commentAuthor)
        .startAt(start)
        .endAt(end)
        .voteType(VoteType.COMMENT)
        .detailVoteType(detailVoteType)
        .positiveCount(1)
        .negativeCount(0)
        .threshold(commentThreshold)
        .build();
        return voteRepository.save(vote);
    }

    @Override
    public Long deleteCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) {
        Vote vote = createCommentVote(user, commentId, commentAuthor, DetailVoteType.DELETE_COMMENT);
        scheduleService.scheduleDeleteCommentVote(vote.getId(), postId, spaceName, commentContent, vote.getEndAt());
        return vote.getId();
    }

    private Long updatePinStatusVote(boolean isPined, User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException {
        if (!commentService.checkCommentPinStatus(commentId, isPined)) {
            throw new BadRequestException(isPined? "该评论已置顶" : "该评论未置顶");
        }
        Vote vote = createCommentVote(user, commentId, commentAuthor, isPined? DetailVoteType.PIN_COMMENT : DetailVoteType.UNPIN_COMMENT);
        scheduleService.scheduleUpdatePinStatusCommentVote(vote.getId(), isPined, commentId, postId, spaceName, commentContent, vote.getEndAt());
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
