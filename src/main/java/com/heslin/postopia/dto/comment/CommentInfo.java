package com.heslin.postopia.dto.comment;

import com.heslin.postopia.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
public class CommentInfo {
    private final Long id;
    private final String content;
    private final Instant createdAt;
    private final Long userId;
    private final String nickName;
    private final String avatar;
    @Setter
    private List<CommentInfo> children = new ArrayList<>();

    public CommentInfo(Long id, String content, Instant createdAt, Long userId, String nickName, String avatar) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = userId = User.maskId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
    }
}
