package com.heslin.postopia.search.repository;

import com.heslin.postopia.search.model.SpaceDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceDocRepository extends ElasticsearchRepository<SpaceDoc, String> {

    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["name^2", "description"],
                "type": "best_fields",
                "tie_breaker": 0.5
              }
            }
            """)
    Page<SpaceDoc> matchSpaceDoc(String keyword, Pageable pageable);
}
