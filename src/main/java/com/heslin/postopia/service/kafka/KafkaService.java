package com.heslin.postopia.service.kafka;

import com.heslin.postopia.dto.post.PostDiff;
import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KafkaService {
    private final KafkaTemplate<Long, Integer> kafkaTemplate;
    private final EntityManager entityManager;

    @Autowired
    public KafkaService(KafkaTemplate<Long, Integer> kafkaTemplate, EntityManager entityManager) {
        this.kafkaTemplate = kafkaTemplate;
        this.entityManager = entityManager;
    }

    private void send(String topic, Long key, Enum value) {
        kafkaTemplate.send(topic, key, value.ordinal());
    }

    public void sendToPost(Long postId, PostOperation value) {
        send("post", postId, value);
    }

    public void sendToComment(Long commentId, CommentOperation value) {
        send("comment", commentId, value);
    }

    public void sendToSpace(Long spaceId, SpaceOperation value) {
        send("space", spaceId, value);
    }

    @KafkaListener(topics = "post", containerFactory = "batchFactory")
    @Transactional
    protected void processPostOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, PostDiff>();
        records.forEach(record -> {
            System.out.println("Processing post operation: " + record.key() + " " + record.value());
            PostDiff postDiff = mp.computeIfAbsent(record.key(), k -> new PostDiff());
            postDiff.upDateDiff(record.value());
        });
        executeBatchPostOperations(mp);
    }

    @Retryable
    @Transactional
    protected void executeBatchPostOperations(HashMap<Long, PostDiff> mp) {
        StringBuilder sql = new StringBuilder("UPDATE posts SET ");
        Map<String, Object> params = mp.keySet().stream().collect(Collectors.toMap(k -> "id" + k, k -> k));
        List<Long> ids = new ArrayList<>(mp.keySet());
        params.put("ids", ids);
        boolean shouldUpdatePositive = mp.values().stream().anyMatch(PostDiff::shouldUpdatePositive);
        boolean shouldUpdateNegative = mp.values().stream().anyMatch(PostDiff::shouldUpdateNegative);
        boolean shouldUpdateComment = mp.values().stream().anyMatch(PostDiff::shouldUpdateComment);

        // positive_count
        if (shouldUpdatePositive) {
            sql.append("positive_count = CASE id ");
            mp.forEach((key, postDiff) -> {
                if (!postDiff.shouldUpdatePositive()) {
                    return;
                }
                sql.append("WHEN :id").append(key).append(" THEN positive_count + :pc").append(key).append(" ");
                params.put("pc" + key, postDiff.getPositiveDiff());
            });
            sql.append(" ELSE positive_count END, ");
        }

        // negative_count
        if (shouldUpdateNegative) {
            sql.append("negative_count = CASE id ");
            mp.forEach((key, postDiff) -> {
                if (!postDiff.shouldUpdateNegative()) {
                    return;
                }
                sql.append("WHEN :id").append(key).append(" THEN negative_count + :nc").append(key).append(" ");
                params.put("nc" + key, postDiff.getNegativeDiff());
            });
            sql.append(" ELSE negative_count END, ");
        }


        if (shouldUpdateComment) {
            sql.append("comment_count = CASE id ");
            mp.forEach((key, postDiff) -> {
                if (!postDiff.shouldUpdateComment()) {
                    return;
                }
                sql.append("WHEN :id").append(key).append(" THEN comment_count + :cc").append(key).append(" ");
                params.put("cc" + key, postDiff.getNegativeDiff());
            });
            sql.append(" ELSE comment_count END, ");
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE id IN (:ids)");

        var query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        query.executeUpdate();
    }
}
