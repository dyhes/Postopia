package com.heslin.postopia.dto.post;

import com.heslin.postopia.enums.OpinionStatus;

public record UserPostSummary(Long postId, String subject, long positiveCount, long negativeCount, long commentCount) {
}