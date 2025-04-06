package com.heslin.postopia.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.diff.CommentDiff;
import com.heslin.postopia.dto.diff.Diff;
import com.heslin.postopia.dto.diff.PostDiff;
import com.heslin.postopia.dto.diff.SpaceDiff;
import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.service.search.ElasticSevice;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KafkaService {
    private final KafkaTemplate<Long, Integer> integerKafkaTemplate;
    private final KafkaTemplate<Long, String> stringKafkaTemplate;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final ElasticSevice elasticSevice;

    @Autowired
    public KafkaService(KafkaTemplate<Long, Integer> integerKafkaTemplate, KafkaTemplate<Long, String> stringKafkaTemplate, EntityManager entityManager, ObjectMapper objectMapper, ElasticSevice elasticSevice) {
        this.integerKafkaTemplate = integerKafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.elasticSevice = elasticSevice;
    }

    public void sendToSpaceCreate(Long key, String value){
        stringKafkaTemplate.send("space_create", key, value);
    }

    @KafkaListener(topics = "space_create", containerFactory = "batchStringFactory")
    @Transactional
    protected void processSpaceCreate(List<ConsumerRecord<Long, String>> records) {
        var docs = records.stream()
        .map(record -> {
            try {
                System.out.println(record.value());
                return objectMapper.readValue(record.value(), SpaceDoc.class);
            } catch (JsonProcessingException e) {
                System.out.println("Error parsing JSON: " + e.getMessage());
                throw new RuntimeException(e);
            }
        })
        .toList();
        elasticSevice.indexSpaces(docs);
    }


    private void send(String topic, Long key, Enum value) {
        integerKafkaTemplate.send(topic, key, value.ordinal());
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

    @KafkaListener(topics = "post", containerFactory = "batchIntegerFactory")
    @Transactional
    protected void processPostOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new PostDiff());
            diff.updateDiff(record.value());
        });
        executeBatchDiffOperations(mp, "posts");
    }

    @KafkaListener(topics = "comment", containerFactory = "batchIntegerFactory")
    @Transactional
    protected void processCommentOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new CommentDiff());
            diff.updateDiff(record.value());
        });
        executeBatchDiffOperations(mp, "comments");
    }

    @KafkaListener(topics = "space", containerFactory = "batchIntegerFactory")
    @Transactional
    protected void processSpaceOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new SpaceDiff());
            diff.updateDiff(record.value());
        });
        executeBatchDiffOperations(mp, "spaces");
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
    protected void executeBatchDiffOperations(HashMap<Long, Diff> mp, String tableName) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        Map<String, Object> params = mp.keySet().stream().collect(Collectors.toMap(k -> "id" + k, k -> k));
        List<Long> ids = new ArrayList<>(mp.keySet());
        params.put("ids", ids);
        boolean shouldUpdatePositive = mp.values().stream().anyMatch(Diff::shouldUpdatePositive);
        boolean shouldUpdateNegative = mp.values().stream().anyMatch(Diff::shouldUpdateNegative);
        boolean shouldUpdateComment = mp.values().stream().anyMatch(Diff::shouldUpdateComment);
        boolean shouldUpdateMember = mp.values().stream().anyMatch(Diff::shouldUpdateMember);

        // positive_count
        buildSql(sql, params, mp, shouldUpdatePositive, Diff::shouldUpdatePositive, Diff::getPositiveDiff, "positive_count");

        // negative_count
        buildSql(sql, params, mp, shouldUpdateNegative, Diff::shouldUpdateNegative, Diff::getNegativeDiff, "negative_count");

        // comment_count
        buildSql(sql, params, mp, shouldUpdateComment, Diff::shouldUpdateComment, Diff::getCommentDiff, "comment_count");

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
