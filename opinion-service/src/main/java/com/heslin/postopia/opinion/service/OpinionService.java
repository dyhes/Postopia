package com.heslin.postopia.opinion.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.CommentOperation;
import com.heslin.postopia.common.kafka.enums.PostOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.dto.VoteOpinionInfo;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.opinion.enums.OpinionType;
import com.heslin.postopia.opinion.redis.OpinionRedisService;
import com.heslin.postopia.opinion.repository.OpinionRepository;
import com.heslin.postopia.opinion.request.UpsertCommentRequest;
import com.heslin.postopia.opinion.request.UpsertPostRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OpinionService{
    private final OpinionRepository opinionRepository;
    private final EntityManager entityManager;
    private final KafkaService kafkaService;
    private final OpinionRedisService redisService;

    @Autowired
    public OpinionService(OpinionRepository opinionRepository, EntityManager entityManager, KafkaService kafkaService, OpinionRedisService redisService) {
        this.opinionRepository = opinionRepository;
        this.entityManager = entityManager;
        this.kafkaService = kafkaService;
        this.redisService = redisService;
    }

    public void deletePostOpinion(Long xUserId, Long id, boolean isPositive) {
        boolean success = opinionRepository.deletePostPinion(id, xUserId, isPositive) > 0;
        if (success) {
            kafkaService.sendToPost(id, isPositive? PostOperation.CANCEL_LIKE : PostOperation.CANCEL_DISLIKE);
        }
    }

    public void deleteCommentOpinion(Long xUserId, Long id, boolean isPositive) {
        boolean success = opinionRepository.deleteCommentPinion(id, xUserId, isPositive) > 0;
        if (success) {
            kafkaService.sendToComment(id, isPositive? CommentOperation.CANCEL_LIKE : CommentOperation.CANCEL_DISLIKE);
        }
    }

    @Transactional
    public void upsertVoteOpinion(Long xUserId, Boolean isPositive, Long voteId) {
        String sql = "INSERT INTO vote_opinions(updated_at, is_positive, user_id, vote_id) " +
        "VALUES (:ua, :ip, :uid, :vid) " +
        "ON CONFLICT (user_id, vote_id) " +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", Instant.now());
        query.setParameter("ip", isPositive);
        query.setParameter("uid", xUserId);
        query.setParameter("vid", voteId);
        kafkaService.sendToUser(xUserId, UserOperation.CREDIT_EARNED);
    }

    public void upsertPostOpinion(Long xUserId, String xUsername, UpsertPostRequest request) {
        String sql = "INSERT INTO post_opinions(updated_at, is_positive, user_id, post_id) " +
        "VALUES (:ua, :ip, :uid, :pid) " +
        "ON CONFLICT (user_id, post_id)" +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", Instant.now());
        query.setParameter("ip", request.isPositive());
        query.setParameter("uid", xUserId);
        query.setParameter("pid", request.postId());
        kafkaService.sendToUser(xUserId, UserOperation.CREDIT_EARNED);
        boolean isInsert = (boolean) query.getSingleResult();
        if (isInsert) {
            kafkaService.sendToPost(request.postId(), request.isPositive()? PostOperation.LIKED : PostOperation.DISLIKED);
            kafkaService.sendToUser(xUserId, UserOperation.CREDIT_EARNED);
            if (request.isPositive()) {
                kafkaService.sendToUser(request.userId(), UserOperation.CREDIT_EARNED);
            }
        } else {
            kafkaService.sendToPost(request.postId(), request.isPositive()? PostOperation.SWITCH_TO_LIKE : PostOperation.SWITCH_TO_DISLIKE);
        }
        redisService.updatePOOpinionAggregation(request.spaceId(), request.postId(), xUserId, xUsername, request.isPositive());
    }

    public void upsertCommentOpinion(Long xUserId, String xUsername, UpsertCommentRequest request) {
        String sql = "INSERT INTO comment_opinions(updated_at, is_positive, user_id, comment_id) " +
        "VALUES (:ua, :ip, :uid, :cid) " +
        "ON CONFLICT (user_id, comment_id) " +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", Instant.now());
        query.setParameter("ip", request.isPositive());
        query.setParameter("uid", xUserId);
        query.setParameter("cid", request.commentId());
        boolean isInsert = (boolean) query.getSingleResult();
        if (isInsert) {
            kafkaService.sendToComment(request.commentId(), request.isPositive()? CommentOperation.LIKED : CommentOperation.DISLIKED );
            kafkaService.sendToUser(xUserId, UserOperation.CREDIT_EARNED);
            if (request.isPositive()) {
                kafkaService.sendToUser(request.userId(), UserOperation.CREDIT_EARNED);
            }
        } else {
            kafkaService.sendToComment(request.commentId(), request.isPositive()? CommentOperation.SWITCH_TO_LIKE : CommentOperation.SWITCH_TO_DISLIKE );
        }
        redisService.updateCOOpinionAggregation(request.spaceId(), request.commentId(), xUserId, xUsername, request.isPositive());
    }

    public void notifyVoter(Long voteId, String message) {
        Stream<VoteOpinionInfo> opinionStream = opinionRepository.findStreamVoteOpinions(voteId);
        opinionStream.forEach(info -> {
            kafkaService.sendMessage(info.userId(), message.formatted(info.isPositive()? "赞成" : "反对"));
        });
    }

    public List<OpinionInfo> getOpinion(Long userId, List<Long> idList, OpinionType opinionType) {
        List<OpinionInfo> opinionInfos;
        switch (opinionType) {
            case POST -> opinionInfos = opinionRepository.getPostOpinion(userId, idList);
            case COMMENT -> opinionInfos = opinionRepository.getCommentOpinion(userId, idList);
            case VOTE -> opinionInfos = opinionRepository.getVoteOpinion(userId, idList);
            default -> throw new IllegalArgumentException("Invalid opinion type: " + opinionType);
        }
        Map<Long, OpinionInfo> mergeIdMap = opinionInfos.stream().collect(Collectors.toMap(OpinionInfo::mergeId, opinionPart -> opinionPart));
        List<OpinionInfo> res = idList.stream().map(id -> mergeIdMap.getOrDefault(id, new OpinionInfo(id, OpinionStatus.NIL, null))).toList();
        return res;
    }
}
