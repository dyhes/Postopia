package com.heslin.postopia.model;

import com.heslin.postopia.model.opinion.CommentOpinion;
import com.heslin.postopia.model.vote.CommentVote;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    public Comment() {

    }

    public Comment(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> children = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @OneToMany(mappedBy = "comment", orphanRemoval = true)
    private Set<CommentOpinion> opinions = new HashSet<>();

    @OneToMany(mappedBy = "comment", orphanRemoval = true)
    private List<CommentVote> votes = new ArrayList<>();

    private long positiveCount;

    private long negativeCount;
}
