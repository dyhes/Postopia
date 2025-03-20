package com.heslin.postopia.dto.comment;

import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
public class CommentInfo {
    private final Long id;
    private final Long parentId;
    private final String content;
    private final Instant createdAt;
    private final Long userId;
    private final String nickName;
    private final String avatar;
    private final OpinionStatus opinion;
    private final List<CommentInfo> children;

    public CommentInfo(Long id, String content, Instant createdAt, Long userId, String nickName, String avatar, OpinionStatus opinion) {
        this.id = id;
        this.parentId = null;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = userId = User.maskId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
        this.opinion = opinion;
        this.children = new ArrayList<>();
    }

    public CommentInfo(Long id, String content, Instant createdAt, Long userId, String nickName, String avatar, OpinionStatus opinion, Long parentId) {
        this.id = id;
        this.parentId = parentId;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = userId = User.maskId(userId);
        this.nickName = nickName;
        this.avatar = avatar;
        this.opinion = opinion;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "CommentInfo{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", userId=" + userId +
                ", nickName='" + nickName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", opinion=" + opinion +
                ", children=" + children +
                '}';
    }
}
