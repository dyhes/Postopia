package com.heslin.postopia.jpa.model;

import com.heslin.postopia.jpa.model.opinion.CommentOpinion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id",foreignKey = @ForeignKey(name = "fk_comment_post", foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_comment", foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE"))
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<Comment> children;

    @CreatedDate
    private Instant createdAt;

    @OneToMany(mappedBy = "comment",cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private Set<CommentOpinion> opinions;

    private long positiveCount;

    private long negativeCount;
}
