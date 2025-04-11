package com.heslin.postopia.kafka;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.elasticsearch.dto.RoutedDocUpdate;
import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.diff.CommentDiff;
import com.heslin.postopia.dto.diff.Diff;
import com.heslin.postopia.dto.diff.PostDiff;
import com.heslin.postopia.dto.diff.SpaceDiff;
import com.heslin.postopia.elasticsearch.model.UserDoc;
import com.heslin.postopia.enums.kafka.CommentOperation;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
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
    private final KafkaTemplate<Long, Integer> liKafkaTemplate;
    private final KafkaTemplate<String, String> ssKafkaTemplate;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public KafkaService(KafkaTemplate<Long, Integer> liKafkaTemplate, KafkaTemplate<String, String> ssKafkaTemplate, EntityManager entityManager, ObjectMapper objectMapper, ElasticsearchOperations elasticsearchOperations) {
        this.liKafkaTemplate = liKafkaTemplate;
        this.ssKafkaTemplate = ssKafkaTemplate;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void sendToDocDelete(String fieldType, String key, String value){
        ssKafkaTemplate.send(fieldType + "_delete", key, value);
    }

    protected <T, C> void processDocDelete(String id, String route,  Class<T> documentClass, String childField, Class<C> childrenClass) {
        // 未传入routing，待优化
        elasticsearchOperations.delete(id, documentClass);
        Query termQuery = NativeQuery.builder().withQuery(QueryBuilders.term(builder -> builder.field(childField).value(id))).withRoute(route).build();
        DeleteQuery deleteQuery = DeleteQuery.builder(termQuery).build();
        elasticsearchOperations.delete(deleteQuery, childrenClass);
    }

    @KafkaListener(topics = "post_delete", containerFactory = "ssFactory")
    @Transactional
    protected void processPostDelete(ConsumerRecord<String, String> record) {
        processDocDelete(record.key(), record.value(), PostDoc.class, "postId", CommentDoc.class);
    }

    @KafkaListener(topics = "comment_delete", containerFactory = "ssFactory")
    @Transactional
    protected void processCommentDelete(ConsumerRecord<String, String> record) {
        processDocDelete(record.key(), record.value(), CommentDoc.class, "parentId", CommentDoc.class);
    }

    public void sendToDocUpdate(String fieldType, String key, String routing, Map<String, Object> update) {
        try {
            String docUpdate = objectMapper.writeValueAsString(update);
            String value = objectMapper.writeValueAsString(new RoutedDocUpdate(routing, docUpdate));
            ssKafkaTemplate.send(fieldType + "_update", key, value);
        } catch (JsonProcessingException e) {
            System.out.println("Kafka send error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected <T> void processDocUpdate(List<ConsumerRecord<String, String>> records, Class<T> documentClass) {
        var queries = records.stream().map(record -> {
            try {
                RoutedDocUpdate docUpdate = objectMapper.readValue(record.value(), RoutedDocUpdate.class);
                return UpdateQuery.builder(record.key())
                        .withRouting(docUpdate.routing())
                        .withDocument(Document.parse(docUpdate.docUpdate()))
                        .build();
            } catch (JsonProcessingException e) {
                System.out.println("Error parsing JSON: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).toList();
        new BulkRequest.Builder();
        elasticsearchOperations.bulkUpdate(queries, documentClass);
    }

    @KafkaListener(topics = "user_update", containerFactory = "batchSSFactory")
    @Transactional
    protected void processUserUpdate(List<ConsumerRecord<String, String>> records) {
        processDocUpdate(records, UserDoc.class);
    }

    @KafkaListener(topics = "post_update", containerFactory = "batchSSFactory")
    @Transactional
    protected void processPostUpdate(List<ConsumerRecord<String, String>> records) {
        processDocUpdate(records, PostDoc.class);
    }

    public void sendToDocCreate(String docType, String key, Object value){
        try {
            ssKafkaTemplate.send(docType + "_create", key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            System.out.println("Kafka send error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private <T> void processDocCreate(List<ConsumerRecord<String, String>> records, Function<T, String> getRouting, Class<T> documentClass) {
        var queries = records.stream()
                .map(record -> {
                    try {
                        System.out.println(record.value());
                        T doc = objectMapper.readValue(record.value(), documentClass);
                        System.out.println(doc);
                        System.out.println(getRouting.apply(doc));
                        return new IndexQueryBuilder()
                                .withId(record.key())
                                .withRouting(getRouting.apply(doc))
                                .withObject(doc)
                                .build();
                    } catch (JsonProcessingException e) {
                        System.out.println("Error parsing JSON: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        elasticsearchOperations.bulkIndex(queries, documentClass);
    }

    @KafkaListener(topics = "space_create", containerFactory = "batchSSFactory")
    @Transactional
    protected void processSpaceCreate(List<ConsumerRecord<String, String>> records) {
        processDocCreate(records, SpaceDoc::getId, SpaceDoc.class);
    }

    @KafkaListener(topics = "user_create", containerFactory = "batchSSFactory")
    @Transactional
    protected void processUserCreate(List<ConsumerRecord<String, String>> records) {
        processDocCreate(records, UserDoc::getId, UserDoc.class);
    }

    @KafkaListener(topics = "post_create", containerFactory = "batchSSFactory")
    @Transactional
    protected void processPostCreate(List<ConsumerRecord<String, String>> records) {
        processDocCreate(records, PostDoc::getSpaceName, PostDoc.class);
    }

    @KafkaListener(topics = "comment_create", containerFactory = "batchSSFactory")
    @Transactional
    protected void processCommentCreate(List<ConsumerRecord<String, String>> records) {
        processDocCreate(records, CommentDoc::getSpaceName, CommentDoc.class);
    }

    private void send(String topic, Long key, Enum value) {
        liKafkaTemplate.send(topic, key, value.ordinal());
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

    @KafkaListener(topics = "post", containerFactory = "batchLIFactory")
    @Transactional
    protected void processPostOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new PostDiff());
            diff.updateDiff(record.value());
        });
        executeBatchDiffOperations(mp, "posts");
    }

    @KafkaListener(topics = "comment", containerFactory = "batchLIFactory")
    @Transactional
    protected void processCommentOperations(List<ConsumerRecord<Long, Integer>> records) {
        var mp = new HashMap<Long, Diff>();
        records.forEach(record -> {
            Diff diff = mp.computeIfAbsent(record.key(), k -> new CommentDiff());
            diff.updateDiff(record.value());
        });
        executeBatchDiffOperations(mp, "comments");
    }

    @KafkaListener(topics = "space", containerFactory = "batchLIFactory")
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
