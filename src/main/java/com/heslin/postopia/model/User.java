package com.heslin.postopia.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;

import com.heslin.postopia.model.opinion.Opinion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false, unique = true)
    private String username;

    private String nickname;

    @Column(nullable = false)
    private String password;

    private String avatar;

    private String bindedWeChat;

    private String bindedAliPay;

    private String bindedGoogle;

    private String bindedGithub;

    @OneToMany(mappedBy = "user")
    private Set<SpaceUserInfo> spaces = new HashSet<>();

    @OneToMany(mappedBy= "poster")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy= "author")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Opinion> opinions = new ArrayList<>();
}