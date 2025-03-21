package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;
import lombok.Data;
import lombok.Getter;


//
@Getter
public class UserPostSummary extends PostSummary{
    private final OpinionStatus opinionStatus;

    public UserPostSummary(Long spaceId, Long postId, String subject, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus) {
        super(spaceId, postId, subject, positiveCount, negativeCount, commentCount);
        this.opinionStatus = opinionStatus;
    }
}