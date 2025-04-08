package com.heslin.postopia.jpa.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Builder
@Table(name = "post_drafts")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PostDraft {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String content;
    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false, updatable=false)
    private User user;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable=false, updatable=false)
    private Space space;
}
