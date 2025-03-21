package com.heslin.postopia.dto.post;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 用户自身视角的自己的帖子摘要
@Getter
public class PostSummary {
    private final Long spaceId;
    private final Long postId;
    private final String subject;
    private final long positiveCount;
    private final long negativeCount;
    private final long commentCount;


    PostSummary(Long spaceId, Long postId, String subject, long positiveCount, long negativeCount, long commentCount){

        this.spaceId = spaceId;
        this.postId = postId;
        this.subject = subject;
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
        this.commentCount = commentCount;
    }
}
