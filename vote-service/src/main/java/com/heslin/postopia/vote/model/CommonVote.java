package com.heslin.postopia.vote.model;


import com.heslin.postopia.vote.enums.VoteType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
name = "common_votes",
indexes = {
@Index(name = "common_type_related_entity_idx", columnList = "common_vote_type, relatedEntity", unique = true),
}
)
public class CommonVote extends Vote {

    @Enumerated(EnumType.ORDINAL)
    private VoteType commonVoteType;
}
