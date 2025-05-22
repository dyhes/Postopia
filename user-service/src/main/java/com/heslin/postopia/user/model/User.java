package com.heslin.postopia.user.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false, unique = true)
    private String username;

    private String nickname;

    @Column(nullable = false)
    private String password;

    private String email;

    private boolean showEmail;

    private String avatar;

    private String introduction;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long postCount = 0L;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long commentCount = 0L;

     @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long credit = 0L;

    @PrePersist
    public void prePersist() {
        if (commentCount == null) {
            commentCount = 0L;
        }
        if (postCount == null) {
            postCount = 0L;
        }
        if (credit == null) {
            credit = 0L;
        }
    }
}