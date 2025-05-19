package com.heslin.postopia.opinion.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "comment_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "comment_opinion_unique", columnNames = {"user_id", "comment_id"}),
})
@NoArgsConstructor
@AllArgsConstructor
public class CommentOpinion extends Opinion {
    private Long commentId;
}
