package com.heslin.postopia.vote.model;

import com.heslin.postopia.vote.enums.DetailVoteType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_generator")
    @TableGenerator(name = "id_generator", table = "id_gen", pkColumnName = "gen_name", valueColumnName = "gen_val")
    private Long id;

    private Instant startAt;

    private Instant endAt;

    @Enumerated(EnumType.STRING)
    private DetailVoteType detailVoteType;

    private Long relatedEntity;

    private Long relatedUser;

    private Long initiator;

    private long positiveCount;

    private long negativeCount;

    private long threshold;

    public boolean isFulfilled() {
        return positiveCount > negativeCount && positiveCount + negativeCount >= threshold;
    }
}
