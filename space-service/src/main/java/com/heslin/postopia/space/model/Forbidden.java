package com.heslin.postopia.space.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "forbiddens",
indexes = {
@Index(name = "idx_space_user_id", columnList = "space_id, user_id", unique = true),
})
@EntityListeners(AuditingEntityListener.class)
public class Forbidden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JoinColumn(name = "space_id", foreignKey = @ForeignKey(name = "fk_sui_space", foreignKeyDefinition = "FOREIGN KEY (space_id) REFERENCES spaces(space_id)"))
    private Long spaceId;

    private Long userId;

    @CreatedDate
    private Instant createdAt;
}