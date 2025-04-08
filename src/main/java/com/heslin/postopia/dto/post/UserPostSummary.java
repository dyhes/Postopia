package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;


//
@Getter
public class UserPostSummary extends PostSummary{
    private final OpinionStatus opinionStatus;

    public UserPostSummary(String spaceName, Long postId, String subject, String subContent, long positiveCount, long negativeCount, long commentCount, OpinionStatus opinionStatus, Instant createdAt) {
        super(spaceName, postId, subject, subContent, positiveCount, negativeCount, commentCount, createdAt);
        this.opinionStatus = opinionStatus;
    }
}