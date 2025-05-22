package com.heslin.postopia.post.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String content;

    private Long userId;

    private Long spaceId;

    private String spaceName;

    private boolean isArchived;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private long positiveCount;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private long negativeCount;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private long commentCount;

    @CreatedDate
    private Instant createdAt;
}
