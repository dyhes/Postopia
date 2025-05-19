package com.heslin.postopia.opinion.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

//@Data
//@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@Table(name="opinions",
//        uniqueConstraints = {
//                @UniqueConstraint(name = "post_opinion_unique", columnNames = {"user_id", "post_id"}),
//                @UniqueConstraint(name = "comment_opinion_unique", columnNames = {"user_id", "comment_id"}),
//                @UniqueConstraint(name = "vote_opinion_unique", columnNames = {"user_id", "vote_id"}),
//        },
//        indexes = {
//                @Index(name = "idx_post_opinion_unique", columnList = "user_id, post_id"),
//                @Index(name = "idx_comment_opinion_unique", columnList = "user_id, comment_id"),
//                @Index(name = "idx_vote_opinion_unique", columnList = "user_id, vote_id")
//        })
@Data
@MappedSuperclass
public abstract class Opinion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isPositive;

    @JoinColumn(name = "user_id")
    private Long userId;

    private Instant updatedAt;
}
