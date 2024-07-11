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
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String nickname;

    @Column(nullable = false)
    private String password;

    private String bindedWeChat;

    private String bindedAliPay;

    private String bindedGoogle;

    private String bindedGithub;

    @ManyToMany(mappedBy = "members")
    private Set<Space> spaces = new HashSet<>();

    @OneToMany(mappedBy= "poster")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy= "author")
    private List<Comment> comments = new ArrayList<>();
}