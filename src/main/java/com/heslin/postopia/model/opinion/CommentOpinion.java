package com.heslin.postopia.model.opinion;

import com.heslin.postopia.model.Comment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "comment_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "comment_opinion_unique", columnNames = {"user_id", "comment_id"}),
})
public class CommentOpinion extends Opinion {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;


    @Override
    public Triple<Long, Long, Long> getFields() {
        return new ImmutableTriple<>(comment.getId(), null, null);
    }

    @Override
    public String getDiscriminator() {
        return "COMMENT";
    }
}
