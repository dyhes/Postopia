package com.heslin.postopia.dto.comment;

import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.enums.OpinionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// 空间中帖子的评论信息

@Getter
@AllArgsConstructor
public class CommentInfo {
    private final Long id;
    private final Long parentId;
    private final String content;
    private final Instant createdAt;
    private final String username;
    private final String nickname;
    private final String userAvatar;
    private final OpinionStatus opinion;
    private final long positiveCount;
    private final long negativeCount;
    private final boolean isPined;
    private final List<CommentInfo> children = new ArrayList<>();
}