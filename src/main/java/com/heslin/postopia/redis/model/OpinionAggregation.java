package com.heslin.postopia.redis.model;


import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@RedisHash("opinion_aggregation")
public class OpinionAggregation {
    @Id
    private String id;
    private String spaceName;
    private Long postId;
    private Long commentId;
    private String negativeUser;
    private String positiveUser;
    private long positiveCount = 0;
    private long negativeCount = 0;
    @TimeToLive private final Long expiration = (long) -1;

    public OpinionAggregation(String id, String spaceName, Long postId, Long commentId) {
        this.id = id;
        this.spaceName = spaceName;
        this.postId = postId;
        this.commentId = commentId;
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
