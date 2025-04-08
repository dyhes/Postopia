package com.heslin.postopia.jpa.model;

import com.heslin.postopia.jpa.model.opinion.CommentOpinion;
import com.heslin.postopia.jpa.model.opinion.PostOpinion;
import com.heslin.postopia.jpa.model.opinion.VoteOpinion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users",
    indexes = {
        @Index(name = "unique_user_name", columnList = "username", unique = true)
    })
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
}