package com.heslin.postopia.common.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.RoutedDocUpdate;
import com.heslin.postopia.common.kafka.enums.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KafkaService {
    private final KafkaTemplate<Long, Integer> liKafkaTemplate;
    private final KafkaTemplate<String, String> ssKafkaTemplate;
    private final KafkaTemplate<Long, String> lsKfafkaTemplate;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaService(KafkaTemplate<Long, Integer> liKafkaTemplate, KafkaTemplate<String, String> ssKafkaTemplate, KafkaTemplate<Long, String> lsKfafkaTemplate, EntityManager entityManager, ObjectMapper objectMapper) {
        this.liKafkaTemplate = liKafkaTemplate;
        this.ssKafkaTemplate = ssKafkaTemplate;
        this.lsKfafkaTemplate = lsKfafkaTemplate;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Long userId, String content) {
        lsKfafkaTemplate.send("msg", userId, content);
    }

    public void sendToPostCascade(Long id) {
        liKafkaTemplate.send("post_cascade", id, 0);
    }

    public void sendToCommentCascade(Long id) {
        liKafkaTemplate.send("comment_cascade", id, 0);
    }

    public void sendToDocDelete(String fieldType, String id, String route){
        System.out.println("sendToDocDelete: " + fieldType + "_delete");
        System.out.println("id: " + id);
        System.out.println("route: " + route);
        ssKafkaTemplate.send(fieldType + "_delete", id, route);
    }

    public void sendToDocUpdate(String fieldType, String key, String routing, Map<String, Object> update) {
        try {
            String docUpdate = objectMapper.writeValueAsString(update);
            String value = objectMapper.writeValueAsString(new RoutedDocUpdate(routing, docUpdate));
            System.out.println("sendToDocUpdate: " + fieldType + "_update");
            System.out.println("key: " + key);
            System.out.println("routing: " + routing);
            System.out.println("value: " + value);
            ssKafkaTemplate.send(fieldType + "_update", key, value);
        } catch (JsonProcessingException e) {
            System.out.println("Kafka send error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void sendToDocCreate(String docType, String key, Object value){
        try {
            System.out.println("sendToDocCreate: " + docType + "_create");
            System.out.println("key: " + key);
            System.out.println("value: " + value);
            ssKafkaTemplate.send(docType + "_create", key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            System.out.println("Kafka send error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void send(String topic, Long key, Enum value) {
        liKafkaTemplate.send(topic, key, value.ordinal());
    }

    public void sendToUser(Long userId, UserOperation operation) {
        send("user", userId, operation);
    }

    public void sendToCommonVote(Long voteId, VoteOperation operation) {
        send("common_vote", voteId, operation);
    }

    public void sendToSpaceVote(Long voteId, VoteOperation operation) {
        send("space_vote", voteId, operation);
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

    private void buildSql(StringBuilder sql, Map<String, Object> params, HashMap<Long, Diff> mp, boolean shouldEnter, Function<Diff, Boolean> shouldApply, Function<Diff, Long> diffCounter, String field) {
        if (shouldEnter) {
            sql.append(field).append(" = CASE id ");
            mp.forEach((key, diff) -> {
                if (shouldApply.apply(diff)) {
                    sql.append("WHEN :id").append(key).append(" THEN ").append(field).append(" + :").append(field).append(key).append(" ");
                    params.put(field + key, diffCounter.apply(diff));
                }
            });
            sql.append("ELSE ").append(field).append(" END, ");
        }
    }

    @Retryable
    @Transactional
    public void executeBatchDiffOperations(HashMap<Long, Diff> mp, String tableName) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        Map<String, Object> params = mp.keySet().stream().collect(Collectors.toMap(k -> "id" + k, k -> k));
        List<Long> ids = new ArrayList<>(mp.keySet());
        params.put("ids", ids);
        boolean shouldUpdatePositive = mp.values().stream().anyMatch(Diff::shouldUpdatePositive);
        boolean shouldUpdateNegative = mp.values().stream().anyMatch(Diff::shouldUpdateNegative);
        boolean shouldUpdateComment = mp.values().stream().anyMatch(Diff::shouldUpdateComment);
        boolean shouldUpdateMember = mp.values().stream().anyMatch(Diff::shouldUpdateMember);
        boolean shouldUpdatePost = mp.values().stream().anyMatch(Diff::shouldUpdatePost);
        boolean shouldUpdateCredit = mp.values().stream().anyMatch(Diff::shouldUpdateCredit);

        // positive_count
        buildSql(sql, params, mp, shouldUpdatePositive, Diff::shouldUpdatePositive, Diff::getPositiveDiff, "positive_count");

        // negative_count
        buildSql(sql, params, mp, shouldUpdateNegative, Diff::shouldUpdateNegative, Diff::getNegativeDiff, "negative_count");

        // credit_count
        buildSql(sql, params, mp, shouldUpdateCredit, Diff::shouldUpdateCredit, Diff::getCreditDiff, "credit");

        // post_count
        buildSql(sql, params, mp, shouldUpdatePost, Diff::shouldUpdatePost, Diff::getPostDiff, "post_count");

        // comment_count
        buildSql(sql, params, mp, shouldUpdateComment, Diff::shouldUpdateComment, Diff::getCommentDiff, "comment_count");

        // member_count
        buildSql(sql, params, mp, shouldUpdateMember, Diff::shouldUpdateMember, Diff::getMemberDiff, "member_count");

        sql.delete(sql.length() - 2, sql.length());
        sql.append(" WHERE id IN (:ids)");

        System.out.println(sql.toString());

        var query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        query.getParameters().forEach(param -> {
            System.out.println(param.getName() + " : " + query.getParameterValue(param.getName()));
        });
        query.executeUpdate();
    }
}
