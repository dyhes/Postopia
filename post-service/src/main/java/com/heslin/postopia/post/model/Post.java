package com.heslin.postopia.post.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String content;

    private Long userId;

    private Long spaceId;

    private boolean isArchived;

    private long positiveCount;

    private long negativeCount;

    private long commentCount;

    @CreatedDate
    private Instant createdAt;
}
