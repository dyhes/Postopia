package com.heslin.postopia.common.redis.repository;

import com.heslin.postopia.common.redis.model.COAggragation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface COAggregationRepository extends CrudRepository<COAggragation, Long>{
}
