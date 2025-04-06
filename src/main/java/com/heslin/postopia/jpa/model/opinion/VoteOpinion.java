package com.heslin.postopia.jpa.model.opinion;

import com.heslin.postopia.jpa.model.vote.Vote;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vote_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "vote_opinion_unique", columnNames = {"user_id", "vote_id"}),
})
public class VoteOpinion extends Opinion {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;

    @Override
    public Triple<Long, Long, Long> getFields() {
        return new ImmutableTriple<>(null, null, vote.getId());
    }

    @Override
    public String getDiscriminator() {
        return "VOTE";
    }
}
