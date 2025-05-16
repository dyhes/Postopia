package com.heslin.postopia.space.repository;

import com.heslin.postopia.space.model.Forbidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ForbiddenRepository extends JpaRepository<Forbidden, Long> {

    @Query("select f.createdAt from Forbidden f where f.userId = ?1 and f.spaceId = ?2")
    Instant findByMemberLog(Long userId, Long spaceId);
}
