package com.heslin.postopia.model.opinion;

import com.heslin.postopia.model.Post;

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
@DiscriminatorValue("POST")
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
