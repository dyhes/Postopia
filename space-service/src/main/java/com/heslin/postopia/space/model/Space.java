package com.heslin.postopia.space.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 定义索引include
@Table(name="spaces",
indexes = {
    @Index(name = "unique_space_name", columnList = "name", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Space {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false, updatable = false)
    private String name;

    @Column(nullable=false)
    private String description;

    @CreatedDate
    private Instant createdAt;

    private boolean isArchived;

    private String avatar;

    private long memberCount;

    private long postCount;
}
