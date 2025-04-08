package com.heslin.postopia.elasticsearch.repository;

import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.annotations.Routing;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentDocRepository extends ElasticsearchRepository<CommentDoc, Long> {


    @Query("""
            {
              "match": {
                "content": "?0"
              }
            }
            """)
    Page<CommentDoc> matchCommentDoc(String query, Pageable pageable);
    // Custom query methods can be defined here if needed
    // For example, you can add a method to find comments by post ID or user ID
    // @Query("{\"match\": {\"postId\": \"?0\"}}")
    // List<CommentDoc> findByPostId(Long postId);
    @Query("""
    {
      "bool": {
        "must": [
          {
            "match": {
                "content": "?0"
              }
          }
        ],
        "filter": [
          { "term": { "spaceName": "?1" } }
        ]
      }
    }
    """)
    Page<CommentDoc> matchCommentDocBySpace(String query, String keyword, Pageable pageable);

    @Query("""
    {
      "bool": {
        "must": [
          {
            "match": {
                "content": "?0"
              }
          }
        ],
        "filter": [
          { "term": { "userName": "?1" } }
        ]
      }
    }
    """)
    Page<CommentDoc> matchCommentDocByUser(String query, String keyword, Pageable pageable);

    @Query("""
    {
      "bool": {
        "must": [
          {
            "match": {
                "content": "?0"
              }
          }
        ],
        "filter": [
          { "term": { "postId": "?1" } }
        ]
      }
    }
    """)
    Page<CommentDoc> matchCommentDocByPost(String query, String keyword, Pageable pageable);
}
