package com.heslin.postopia.vote.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.vote.feign.CommentFeign;
import com.heslin.postopia.vote.feign.OpinionFeign;
import com.heslin.postopia.vote.feign.PostFeign;
import com.heslin.postopia.vote.feign.SpaceFeign;
import com.heslin.postopia.vote.model.CommonVote;
import com.heslin.postopia.vote.model.SpaceVote;
import com.heslin.postopia.vote.model.Vote;
import com.heslin.postopia.vote.repository.CommonVoteRepository;
import com.heslin.postopia.vote.repository.SpaceVoteRepository;
import com.heslin.postopia.vote.request.CommentVoteRequest;
import com.heslin.postopia.vote.request.PostVoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.function.Function;

@Service
public class VoteScheduleService {
//    private final OpinionRepository opinionRepository;
//    private final SpaceService spaceService;
//    private final MessageService messageService;
//    private final CommentService commentService;
//    private final PostService postService;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final CommonVoteRepository commonVoteRepository;
    private final SpaceVoteRepository spaceVoteRepository;
    private final SpaceFeign spaceFeign;
    private final OpinionFeign opinionFeign;
    private final PostFeign postFeign;
    private final CommentFeign commentFeign;
    private final KafkaService kafkaService;

    @Autowired
    public VoteScheduleService(ThreadPoolTaskScheduler taskScheduler, CommonVoteRepository commonVoteRepository, SpaceVoteRepository spaceVoteRepository, SpaceFeign spaceFeign, OpinionFeign opinionFeign, PostFeign postFeign, CommentFeign commentFeign, KafkaService kafkaService) {
        this.taskScheduler = taskScheduler;
        this.commonVoteRepository = commonVoteRepository;
        this.spaceVoteRepository = spaceVoteRepository;
        this.spaceFeign = spaceFeign;
        this.opinionFeign = opinionFeign;
        this.postFeign = postFeign;
        this.commentFeign = commentFeign;
        this.kafkaService = kafkaService;
    }

    @Transactional
    protected void scheduledAction(boolean isCommon, Long voteId, String voteActionMessage, String relatedUserMessage, Function<Long, Void> voteAction) {
        Vote vote = isCommon? commonVoteRepository.findById(voteId).orElseThrow() : spaceVoteRepository.findById(voteId).orElseThrow();
        System.out.println("here");
        System.out.println(vote.getId());
        String tail;
        if (vote.isFulfilled()) {
            voteAction.apply(vote.getRelatedEntity());
            tail = "的投票已成功通过";
            if (relatedUserMessage != null) {
                kafkaService.sendMessage(vote.getRelatedUser(), relatedUserMessage);
            }
        } else {
            tail = "的投票未通过, 赞成人数 %d，反对人数 %d, 所需最少投票人数 %d".formatted(vote.getPositiveCount(), vote.getNegativeCount(), vote.getThreshold());
        }
        kafkaService.sendMessage(vote.getInitiator(), "您发起的%s%s".formatted(voteActionMessage, tail));
        opinionFeign.notifyVoter(voteId, "您%s的" + voteActionMessage + tail);
        if (vote instanceof CommonVote) {
            commonVoteRepository.delete((CommonVote) vote);
        } else {
            spaceVoteRepository.delete((SpaceVote) vote);
        }
    }

    public void scheduleUpdateSpaceVote(Long voteId, String spaceMessage, String description, String avatar, Instant endAt) {
        String voteActionMessage = "修改%s信息".formatted(spaceMessage);
        taskScheduler.schedule(
        () -> {
            scheduledAction(false, voteId, voteActionMessage, null, sid -> {
                spaceFeign.updateInfo(sid, description, avatar);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleExpelSpaceUserVote(Long voteId, String spaceMessage, Long userId, String username, String reason, Instant endAt) {
        String userMessage = PostopiaFormatter.formatUser(userId, username);
        String expelMessage = "因%s被投票从%s驱逐".formatted(reason, spaceMessage);
        taskScheduler.schedule(
        () -> {
            scheduledAction(false, voteId, "因%s从%s驱逐%s".formatted(reason, spaceMessage, userMessage), "您已%s".formatted(expelMessage), sid -> {
                spaceFeign.expelUser(sid, userId, userMessage + expelMessage);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleMuteSpaceUserVote(Long voteId, String spaceMessage, Long userId, String username, String reason, Instant endAt) {
        String userMessage = PostopiaFormatter.formatUser(userId, username);
        String muteMessage = "因%s被投票在%s中禁言7天".formatted(reason, spaceMessage);
        taskScheduler.schedule(
        () -> {
            scheduledAction(false, voteId, "因%s在%s中禁言%s".formatted(reason, spaceMessage, userMessage), "您已%s".formatted(muteMessage), sid -> {
                spaceFeign.muteUser(sid, userId, userMessage + muteMessage);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleUpdatePostArchiveStatusVote(Long voteId, boolean isArchived, PostVoteRequest request, Instant endAt) {
        String postMessage = "帖子：%s%s".formatted(request.postSubject(), PostopiaFormatter.formatPost(request.spaceId(), request.postId()));
        String voteActionMessage = "%s归档%s".formatted(isArchived ? "" : "取消", postMessage);
        String relatedUserMessage = "您的%s已被投票%s归档".formatted(postMessage, isArchived ? "" : "取消");
        taskScheduler.schedule(
        () -> {
            scheduledAction(true ,voteId, voteActionMessage, relatedUserMessage, pid -> {
                postFeign.updateArchiveStatus(request.postId(), isArchived);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleUpdateCommentPinStatusVote(Long voteId, boolean isPined, CommentVoteRequest request, Instant endAt) {
        String commentMessage = "评论：%s%s".formatted(request.commentContent(), PostopiaFormatter.formatComment(request.spaceId(), request.postId(), request.commentId()));
        String voteActionMessage = "%s置顶%s".formatted(isPined ? "" : "取消", commentMessage);
        String relatedUserMessage = "您的%s已被投票%s置顶".formatted(commentMessage, isPined ? "" : "取消");
        taskScheduler.schedule(
        () -> {
            scheduledAction(true,voteId, voteActionMessage, relatedUserMessage, cid -> {
                commentFeign.updatePinStatus(request.commentId(), isPined);
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleDeleteCommentVote(Long voteId, CommentVoteRequest request, Instant endAt) {
        taskScheduler.schedule(
        () -> {
            scheduledAction(true,voteId, "删除评论 %s".formatted(request.commentContent()), "您的评论：%s 已被投票删除".formatted(request.commentContent()), commentId -> {
                commentFeign.deleteComment(request.spaceId(), request.postId(), commentId, request.userId());
                return null;
            });
        },
        endAt
        );
    }

    public void scheduleDeletePostVote(Long voteId, PostVoteRequest request, Instant endAt) {
        String postMessage = "帖子: %s ".formatted(request.postSubject());
        taskScheduler.schedule(
        () -> {
            scheduledAction(true, voteId, "删除%s".formatted(postMessage), "您的%s已被投票删除".formatted(postMessage), postId -> {
                postFeign.deletePost(postId, request.spaceId(), request.userId());
                return null;
            });
        },
        endAt
        );
    }



}
