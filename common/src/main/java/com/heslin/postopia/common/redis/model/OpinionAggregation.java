package com.heslin.postopia.common.redis.model;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class OpinionAggregation {
    private Long spaceId;
    private String negativeUser;
    private String positiveUser;
    private long positiveCount = 0;
    private long negativeCount = 0;

    OpinionAggregation(Long spaceId) {
        this.spaceId = spaceId;
    }

    public void update(String username, boolean isPositive) {
        if (isPositive) {
            positiveCount++;
            positiveUser = username;
        } else {
            negativeCount++;
            negativeUser = username;
        }
    }
}
