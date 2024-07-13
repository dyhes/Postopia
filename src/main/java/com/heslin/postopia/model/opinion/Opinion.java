package com.heslin.postopia.model.opinion;

import java.time.Instant;

import com.heslin.postopia.model.User;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="opinions")
@DiscriminatorColumn(name="opinion_type")
public class Opinion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    private boolean isPositive;

    @ManyToOne(fetch=FetchType.LAZY)
    private User user;
}
