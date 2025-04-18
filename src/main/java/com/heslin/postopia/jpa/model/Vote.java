package com.heslin.postopia.jpa.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.heslin.postopia.enums.DetailVoteType;
import com.heslin.postopia.enums.VoteType;
import com.heslin.postopia.jpa.model.opinion.VoteOpinion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "votes",
    indexes = {
        @Index(name = "type_related_id_idx", columnList = "vote_type, related_id", unique = true),
    }
)
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant startAt;

    private Instant endAt;

    private Long relatedId;

    @Enumerated(EnumType.ORDINAL)
    private VoteType voteType;

    @Enumerated(EnumType.STRING)
    private DetailVoteType detailVoteType;

    private String relatedUser;

    private String initiator;

    private long positiveCount;

    private long negativeCount;

    private long threshold;

    @OneToMany(mappedBy = "vote", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private Set<VoteOpinion> opinions;

    public boolean isFulfilled() {
        return positiveCount > negativeCount && positiveCount + negativeCount >= threshold;
    }
}
