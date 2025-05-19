package com.heslin.postopia.opinion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vote_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "vote_opinion_unique", columnNames = {"user_id", "vote_id"}),
})
public class VoteOpinion extends Opinion {
    private Long voteId;

    public String getAltitude() {
        return isPositive() ? "赞成" : "反对";
    }
}
