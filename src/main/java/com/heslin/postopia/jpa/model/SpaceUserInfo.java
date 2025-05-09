package com.heslin.postopia.jpa.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "space_user_infos",
    indexes = {
        @Index(name = "idx_space_user", columnList = "space_id, user_id", unique = true),
        @Index(name = "idx_space_user_name", columnList = "space_name, username", unique = true),
    })
@EntityListeners(AuditingEntityListener.class)
public class SpaceUserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Column
    @JoinColumn(name = "space_name", foreignKey = @ForeignKey(name = "fk_sui_space", foreignKeyDefinition = "FOREIGN KEY (space_name) REFERENCES spaces(space_name)"))
    private String spaceName;

    @Column
    @JoinColumn(name = "username", foreignKey = @ForeignKey(name = "fk_sui_user", foreignKeyDefinition = "FOREIGN KEY (username) REFERENCES users(username)"))
    private String username;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    private LocalDate lastActiveAt;

    private Instant muteUntil;

    boolean isMuted() {
        return muteUntil != null && muteUntil.isAfter(Instant.now());
    }
}
