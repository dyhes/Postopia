package com.heslin.postopia.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @ManyToMany
    @JoinTable(
        name="space_members",
        joinColumns={@JoinColumn(name="space_id")},
        inverseJoinColumns={@JoinColumn(name="user_id")})
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy="space")
    private List<Post> posts = new ArrayList<>();
    // created date
}
