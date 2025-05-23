package com.heslin.postopia.search.repository;

import com.heslin.postopia.search.model.PostDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
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
                "spaceId": "?1"
              }
            },
            {
              "term": {
                "_routing": "?1"
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
            "userId": "?1"
          }
        }
      ]
    }
  }
  """)
    Page<PostDoc> matchPostDocByUser(String query, String keyword, Pageable pageable);
}
