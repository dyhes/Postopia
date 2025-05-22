package com.heslin.postopia.common.redis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.redis.core.RedisHash;

@EqualsAndHashCode(callSuper = true)
@Data
@RedisHash("co_aggregation")
public class COAggragation extends OpinionAggregation{
    public COAggragation(Long id, Long spaceId) {
        super(id, spaceId);
    }
}
