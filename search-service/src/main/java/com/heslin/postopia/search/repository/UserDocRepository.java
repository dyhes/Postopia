package com.heslin.postopia.search.repository;

import com.heslin.postopia.search.model.UserDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDocRepository extends ElasticsearchRepository<UserDoc, String> {

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["name", "nickname"],
                "type": "best_fields",
                "tie_breaker": 1.0
              }
            }
            """)
    Page<UserDoc> matchUserDoc(String query, Pageable pageable);
}
