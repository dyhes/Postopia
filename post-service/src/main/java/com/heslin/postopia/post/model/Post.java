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
    private Long positiveCount = 0L;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long negativeCount = 0L;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long commentCount = 0L;

    @PrePersist
    public void prePersist() {
        if (positiveCount == null) {
            positiveCount = 0L;
        }
        if (negativeCount == null) {
            negativeCount = 0L;
        }
        if (commentCount == null) {
            commentCount = 0L;
        }
    }

    @CreatedDate
    private Instant createdAt;
}
