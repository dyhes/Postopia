package com.heslin.postopia.search.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Routing;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "comments")
@Routing("spaceId")
public class CommentDoc {
    @Id
    private Long id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Keyword)
    private String spaceId;
    @Field(type = FieldType.Keyword)
    private String postId;
//    @Field(type = FieldType.Keyword)
//    private String parentId;
    @Field(type = FieldType.Keyword)
    private String userId;
}
