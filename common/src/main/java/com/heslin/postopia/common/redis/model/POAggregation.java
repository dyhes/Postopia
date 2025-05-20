package com.heslin.postopia.common.redis.model;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("po_aggregation")
public class POAggregation extends OpinionAggregation {
    public POAggregation(Long id, Long spaceId) {
        super(id, spaceId);
    }
}
