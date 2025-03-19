package com.heslin.postopia.model.opinion;

import com.heslin.postopia.model.vote.Vote;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("VOTE")
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
