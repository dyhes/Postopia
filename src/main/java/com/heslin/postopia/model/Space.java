package com.heslin.postopia.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;

import com.heslin.postopia.model.vote.SpaceVote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="spaces")
public class Space {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    private String name;
    
    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private Long memberCount;

    @Column(nullable=false)
    private Long postCount;

    @CreatedDate
    private Instant createdAt;

    private boolean isArchived;

    private String avatar;

    @OneToMany(mappedBy="space")
    private Set<SpaceUserInfo> users = new HashSet<>();

    @OneToMany(mappedBy="space")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy="space")
    private List<SpaceVote> votes = new ArrayList<>();
}
