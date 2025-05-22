package com.heslin.postopia.common.redis.model;

import com.heslin.postopia.common.utils.PostopiaFormatter;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class OpinionAggregation {
    @Id
    private Long id;
    private Long spaceId;
    private Long positiveId;
    private String positiveUser;
    private Long negativeId;
    private String negativeUser;
    private long positiveCount = 0;
    private long negativeCount = 0;

    OpinionAggregation(Long id, Long spaceId) {
        this.id = id;
        this.spaceId = spaceId;
    }

    public void update(Long userId, String username, boolean isPositive) {
        if (isPositive) {
            this.positiveCount++;
            this.positiveId = userId;
            this.positiveUser = username;
        } else {
            this.negativeCount++;
            this.negativeId = userId;
            this.negativeUser = username;
        }
    }

    public void buildMessage(StringBuilder messageBuilder) {
        if (positiveCount > 0) {
            messageBuilder.append(PostopiaFormatter.formatUser(positiveId, positiveUser));
            if (positiveCount > 1) {
                messageBuilder.append("等 %d 人赞同".formatted(positiveCount));
            }
        }
        if (positiveId > 0 && negativeCount > 0) {
            messageBuilder.append("，");
        }
        if (negativeCount > 0) {
            messageBuilder.append(PostopiaFormatter.formatUser(negativeId, negativeUser));
            if (positiveCount > 1) {
                messageBuilder.append("等 %d 人反对".formatted(negativeCount));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public Long getSpaceId() {
        return spaceId;
    }
}
