package com.heslin.postopia.service.opinion;

import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import com.heslin.postopia.model.opinion.Opinion;
import com.heslin.postopia.model.opinion.PostOpinion;
import com.heslin.postopia.repository.OpinionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class OpinionServiceImpl implements OpinionService {
    private final OpinionRepository opinionRepository;
    private final EntityManager entityManager;

    @Autowired
    public OpinionServiceImpl(OpinionRepository opinionRepository, EntityManager entityManager) {
        this.opinionRepository = opinionRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public boolean upsertOpinion(Opinion opinion) {
        var tp = opinion.getFields();
        var updateAt = Instant.now();
        return switch (opinion.getDiscriminator()) {
            case "COMMENT" -> upsertCommentOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getLeft());
            case "POST" -> upsertPostOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getMiddle());
            case "VOTE" -> upsertVoteOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getRight());
            default -> throw new IllegalArgumentException("Unknown opinion type: " + opinion.getDiscriminator());
        };
    }

    @Transactional
    protected boolean upsertVoteOpinion(Instant updatedAt, Boolean isPositive, Long userId, Long voteId) {
        String sql = "INSERT INTO vote_opinions(updated_at, is_positive, user_id, vote_id) " +
        "VALUES (:ua, :ip, :uid, :vid) " +
        "ON CONFLICT (user_id, vote_id) " +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", updatedAt);
        query.setParameter("ip", isPositive);
        query.setParameter("uid", userId);
        query.setParameter("vid", voteId);
        return (boolean) query.getSingleResult();
    }

    @Transactional
    protected boolean upsertPostOpinion(Instant updatedAt, Boolean isPositive, Long userId, Long postId) {
        String sql = "INSERT INTO post_opinions(updated_at, is_positive, user_id, post_id) " +
        "VALUES (:ua, :ip, :uid, :pid) " +
        "ON CONFLICT (user_id, post_id)" +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", updatedAt);
        query.setParameter("ip", isPositive);
        query.setParameter("uid", userId);
        query.setParameter("pid", postId);
        return (boolean) query.getSingleResult();
    }

    @Transactional
    protected boolean upsertCommentOpinion(Instant updatedAt, Boolean isPositive, Long userId, Long commentId) {
        String sql = "INSERT INTO comment_opinions(updated_at, is_positive, user_id, comment_id) " +
        "VALUES (:ua, :ip, :uid, :cid) " +
        "ON CONFLICT (user_id, comment_id) " +
        "DO UPDATE SET updated_at = EXCLUDED.updated_at, is_positive = EXCLUDED.is_positive " +
        "RETURNING CASE WHEN xmax = 0 THEN true ELSE false END AS is_insert";
        Query query = entityManager.createNativeQuery(sql, Boolean.class);
        query.setParameter("ua", updatedAt);
        query.setParameter("ip", isPositive);
        query.setParameter("uid", userId);
        query.setParameter("cid", commentId);
        return (boolean) query.getSingleResult();
    }

    @Override
    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
        return opinionRepository.getCommentOpinionsByUser(id, statuses, pageable);
    }

    @Override
    public Page<UserOpinionPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
        return opinionRepository.getPostOpinionsByUser(id, statuses, pageable);
    }
}
