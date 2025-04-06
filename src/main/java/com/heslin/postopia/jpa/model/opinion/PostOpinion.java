package com.heslin.postopia.jpa.model.opinion;

import com.heslin.postopia.jpa.model.Post;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_opinions", uniqueConstraints = {
        @UniqueConstraint(name = "post_opinion_unique", columnNames = {"user_id", "post_id"}),
})
public class PostOpinion extends Opinion {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Override
    public Triple<Long, Long, Long> getFields() {
        return new ImmutableTriple<>(null, post.getId(), null);
    }

    @Override
    public String getDiscriminator() {
        return "POST";
    }
}
