package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.jpa.model.Forbidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ForbiddenRepository extends JpaRepository<Forbidden, Long> {
    @Query("select f.createdAt from Forbidden f where f.spaceId = ?1 and f.username = ?2")
    Instant findLog(Long spaceId, String username);
}
