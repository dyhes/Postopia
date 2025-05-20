package com.heslin.postopia.common.redis.model;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("co_aggregation")
public class COAggragation extends OpinionAggregation{
    public COAggragation(Long id, Long spaceId) {
        super(id, spaceId);
    }
}
