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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member_logs",
indexes = {
    @Index(name = "m_idx_space_user_id", columnList = "space_id, user_id", unique = true),
})
@EntityListeners(AuditingEntityListener.class)
public class MemberLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JoinColumn(name = "space_id", foreignKey = @ForeignKey(name = "fk_sui_space", foreignKeyDefinition = "FOREIGN KEY (space_id) REFERENCES spaces(id)"))
    private Long spaceId;

    @Column
    private Long userId;

    private String username;

    @CreatedDate
    private Instant createdAt;

    private Instant muteUntil;

    public boolean isMuted() {
        return muteUntil != null && muteUntil.isAfter(Instant.now());
    }
}