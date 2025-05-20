package com.heslin.postopia.common.redis.repository;

import com.heslin.postopia.common.redis.model.POAggregation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface POAggregationRepository extends CrudRepository<POAggregation, Long>{
}
