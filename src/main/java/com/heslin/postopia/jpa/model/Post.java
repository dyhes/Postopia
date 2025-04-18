package com.heslin.postopia.jpa.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.heslin.postopia.jpa.model.opinion.PostOpinion;

import lombok.Data;

@Data
@Entity
@AllArgsConstructor
@Builder
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {

    public Post() {

    }

    public Post(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(updatable=false)
    private Long id;
    
    @Column(nullable=false)
    private String subject;

    private String content;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false, updatable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable=false, updatable=false)
    private Space space;
    
    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<Comment> comments;

    @CreatedDate
    private Instant createdAt;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private Set<PostOpinion> opinions;

    private boolean isArchived;

    private long positiveCount;

    private long negativeCount;

    private long commentCount;
}
