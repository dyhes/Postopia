package com.heslin.postopia.model.vote;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.heslin.postopia.model.opinion.VoteOpinion;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "votes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vote_type")
public abstract class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    @OneToMany(mappedBy = "vote", orphanRemoval = true)
    private Set<VoteOpinion> opinions = new HashSet<>();

    private long positiveCount;

    private long negativeCount;
}
