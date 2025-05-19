package com.heslin.postopia.common.redis.repository;

import com.heslin.postopia.common.redis.model.OpinionAggregation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpinionAggregationRepository extends CrudRepository<OpinionAggregation, String>{
}
