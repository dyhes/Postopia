package com.heslin.postopia.opinion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "post_opinion_unique", columnNames = {"user_id", "post_id"}),
})
public class PostOpinion extends Opinion {
    private Long postId;
}
