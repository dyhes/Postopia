package com.heslin.postopia.model;

import com.heslin.postopia.model.opinion.CommentOpinion;
import com.heslin.postopia.model.opinion.Opinion;
import com.heslin.postopia.model.opinion.PostOpinion;
import com.heslin.postopia.model.opinion.VoteOpinion;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
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

    private String email;

    private boolean showEmail;

    private String avatar;

    private String bindedWeChat;

    private String bindedAliPay;

    private String bindedGoogle;

    private String bindedGithub;

    @OneToMany(mappedBy = "user")
    private Set<SpaceUserInfo> spaces = new HashSet<>();

    @OneToMany(mappedBy= "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PostOpinion> postOpinions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<CommentOpinion> commentOpinions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<VoteOpinion> voteOpinions = new ArrayList<>();


    @Override
    public String toString() {
        return "User" + " [id=" + id + "]";
    }

    public static Long maskId(Long id) {
        return id ^ 0x5A5A5A5A5A5A5A5AL;
    }
}