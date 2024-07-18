package com.heslin.postopia.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.heslin.postopia.model.opinion.PostOpinion;
import com.heslin.postopia.model.vote.PostVote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String subject;

    @Column(nullable=false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;
    
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private Set<PostOpinion> opinions = new HashSet<>();

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<PostVote> votes = new ArrayList<>();

    private Long positiveCount;

    private Long negativeCount;
}
