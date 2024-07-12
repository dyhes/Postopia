package com.heslin.postopia.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Post {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String subject;

    @Column(nullable=false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "poster_id")
    private User poster;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;
    
    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;
}
