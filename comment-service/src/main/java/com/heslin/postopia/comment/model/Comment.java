package com.heslin.postopia.comment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String content;

    Long userId;

    Long postId;

    Long spaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_comment", foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE"))
    private Comment parent;

    private boolean isPined;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long positiveCount = 0L;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long negativeCount = 0L;

    @PrePersist
    public void prePersist() {
        if (positiveCount == null) {
            positiveCount = 0L;
        }
        if (negativeCount == null) {
            negativeCount = 0L;
        }
    }

    @CreatedDate
    private Instant createdAt;
}
