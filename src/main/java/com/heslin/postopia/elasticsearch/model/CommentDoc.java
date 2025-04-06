package com.heslin.postopia.elasticsearch.model;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@Document(indexName = "comments")
// routingid = spacename
public class CommentDoc {
    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Keyword, index = false)
    private Long postId;
    @Field(type = FieldType.Keyword, index = false)
    private String userName;
    @Field(type = FieldType.Keyword, index = false)
    private String spaceName;
    @Field(type = FieldType.Text, index = false)
    private String spaceAvatar;
    @Field(type = FieldType.Long, index = false, docValues = false)
    private long opinionCount;
    @Field(type = FieldType.Date, index = false, docValues = false)
    private Instant createdAt;
}
