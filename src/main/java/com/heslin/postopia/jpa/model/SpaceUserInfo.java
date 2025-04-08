package com.heslin.postopia.jpa.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Entity
@Table(name = "space_user_infos",
        indexes = {
                @Index(name = "idx_space_user", columnList = "space_id, user_id", unique = true),
        })
@EntityListeners(AuditingEntityListener.class)
public class SpaceUserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    private LocalDate lastActiveAt;
}
