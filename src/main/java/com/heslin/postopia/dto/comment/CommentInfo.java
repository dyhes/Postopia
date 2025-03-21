package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// 空间中帖子的评论信息

@Getter
public class CommentInfo {
    private final Long id;
    private final Long parentId;
    private final String content;
    private final Instant createdAt;
    private final UserId userId;
    private final String nickName;
    private final String avatar;
    private final OpinionStatus opinion;
    private final long positiveCount;
    private final long negativeCount;
    private final List<CommentInfo> children;

    public CommentInfo(Long id, String content, Instant createdAt, Long userId, String nickName, String avatar, OpinionStatus opinion, long positiveCount, long negativeCount) {
        this.id = id;
        this.parentId = null;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = new UserId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
        this.opinion = opinion;
        this.children = new ArrayList<>();
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }

    public CommentInfo(Long id, String content, Instant createdAt, Long userId, String nickName, String avatar, OpinionStatus opinion, Long parentId, long positiveCount, long negativeCount) {
        this.id = id;
        this.parentId = parentId;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = new UserId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
        this.opinion = opinion;
        this.children = new ArrayList<>();
        this.positiveCount = positiveCount;
        this.negativeCount = negativeCount;
    }
}
