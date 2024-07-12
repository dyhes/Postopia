package com.heslin.postopia.model.vote;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.heslin.postopia.model.opinion.VoteOpinion;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@MappedSuperclass
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    @OneToMany(mappedBy = "vote", orphanRemoval = true)
    private Set<VoteOpinion> opinions = new HashSet<>();

    private Long positiveCount;

    private Long negativeCount;
}
