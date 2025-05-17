package com.heslin.postopia.vote.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(
name = "space_votes"
)
public class SpaceVote extends Vote {
    // username
    // description
    String first;

    //reason
    //avatar
    String second;
}
