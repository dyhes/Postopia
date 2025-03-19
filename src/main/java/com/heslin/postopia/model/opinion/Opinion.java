package com.heslin.postopia.model.opinion;

import java.time.Instant;

import com.heslin.postopia.model.User;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="opinions",
        uniqueConstraints = {
                @UniqueConstraint(name = "post_opinion_unique", columnNames = {"user_id", "post_id"}),
                @UniqueConstraint(name = "comment_opinion_unique", columnNames = {"user_id", "comment_id"}),
                @UniqueConstraint(name = "vote_opinion_unique", columnNames = {"user_id", "vote_id"}),
//        },
//        indexes = {
//                @Index(name = "idx_post_opinion_unique", columnList = "user_id, post_id"),
//                @Index(name = "idx_comment_opinion_unique", columnList = "user_id, comment_id"),
//                @Index(name = "idx_vote_opinion_unique", columnList = "user_id, vote_id")
        })
@DiscriminatorColumn(name="opinion_type")
public abstract class Opinion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant updatedAt;

    private boolean isPositive;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public abstract Triple<Long, Long, Long> getFields();

    public abstract String getDiscriminator();
}
