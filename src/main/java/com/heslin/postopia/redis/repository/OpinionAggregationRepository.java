package com.heslin.postopia.redis.repository;

import com.heslin.postopia.redis.model.OpinionAggregation;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpinionAggregationRepository extends CrudRepository<OpinionAggregation, String>{
}
