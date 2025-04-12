package com.heslin.postopia.redis.repository;

import com.heslin.postopia.redis.model.OpinionAggregation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpinionAggregationRepository extends CrudRepository<OpinionAggregation, String> {
}
