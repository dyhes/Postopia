package com.heslin.postopia.elasticsearch.repository;

import com.heslin.postopia.elasticsearch.model.PostDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.annotations.Routing;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostDocRepository extends ElasticsearchRepository<PostDoc, Long> {

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["subject^2", "content"],
                "type": "best_fields",
                "tie_breaker": 0.5
              }
            }
            """)
    Page<PostDoc> matchPostDoc(String query, Pageable pageable);


    @Query("""
        {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": "?0",
                "fields": [
                  "subject^2",
                  "content"
                ],
                "type": "best_fields",
                "tie_breaker": 0.5
              }
            }
          ],
          "filter": [
            {
              "term": {
                "spaceName": "?1",
                "_routing": [ "?1" ]
              }
            }
          ]
        }
    }
    """)
    Page<PostDoc> matchPostDocBySpace(String query, String keyword, Pageable pageable);

    @Query("""
    {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "?0",
            "fields": [
              "subject^2",
              "content"
            ],
            "type": "best_fields",
            "tie_breaker": 0.5
          }
        }
      ],
      "filter": [
        {
          "term": {
            "username": "?1"
          }
        }
      ]
    }
  }
  """)
    Page<PostDoc> matchPostDocByUser(String query, String keyword, Pageable pageable);
}
