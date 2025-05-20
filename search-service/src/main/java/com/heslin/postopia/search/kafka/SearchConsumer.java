package com.heslin.postopia.search.kafka;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.RoutedDocUpdate;
import com.heslin.postopia.search.model.CommentDoc;
import com.heslin.postopia.search.model.PostDoc;
import com.heslin.postopia.search.model.SpaceDoc;
import com.heslin.postopia.search.model.UserDoc;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Function;
import java.util.List;

@Component
public class SearchConsumer {
    private final ObjectMapper objectMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public SearchConsumer(ObjectMapper objectMapper, ElasticsearchOperations elasticsearchOperations) {
        this.objectMapper = objectMapper;
        this.elasticsearchOperations = elasticsearchOperations;
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
        processDocCreate(records, PostDoc::getSpaceId, PostDoc.class);
    }

    @KafkaListener(topics = "comment_create", containerFactory = "batchSSFactory")
    @Transactional
    protected void processCommentCreate(List<ConsumerRecord<String, String>> records) {
        processDocCreate(records, CommentDoc::getSpaceId, CommentDoc.class);
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
        //new BulkRequest.Builder();
        elasticsearchOperations.bulkUpdate(queries, documentClass);
    }

    @KafkaListener(topics = "space_update", containerFactory = "batchSSFactory")
    @Transactional
    protected void processSpaceUpdate(List<ConsumerRecord<String, String>> records) {
        processDocUpdate(records, SpaceDoc.class);
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

}
