package com.heslin.postopia.comment.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String content;

    Long userId;

    Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_comment", foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE"))
    private Comment parent;

    private boolean isPined;

    @Column(columnDefinition = "bigint default 0")
    private long positiveCount;

    @Column(columnDefinition = "bigint default 0")
    private long negativeCount;

    @CreatedDate
    private Instant createdAt;
}
