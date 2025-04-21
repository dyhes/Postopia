package com.heslin.postopia.jpa.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Data
@Table(name = "forbiddens",
    indexes = {
        @Index(name = "idx_space_user_name", columnList = "space_name, username", unique = true),
    })
@EntityListeners(AuditingEntityListener.class)
public class Forbidden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JoinColumn(name = "space_name", foreignKey = @ForeignKey(name = "fk_sui_space", foreignKeyDefinition = "FOREIGN KEY (space_name) REFERENCES spaces(space_name)"))
    private String spaceName;

    @Column
    @JoinColumn(name = "username", foreignKey = @ForeignKey(name = "fk_sui_user", foreignKeyDefinition = "FOREIGN KEY (username) REFERENCES users(username)"))
    private String username;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;
}
