package com.heslin.postopia.space.repository;

import com.heslin.postopia.space.model.MemberLog;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MemberRepository extends JpaRepository<MemberLog, Long> {
    @Transactional
    @Modifying
    @Query("delete from MemberLog m where m.spaceId = :spaceId and m.userId = :userId")
    int deleteBySpaceIdAndUserId( @Param("userId") Long userId, @Param("spaceId") Long spaceId);

    Page<MemberLog> findBySpaceIdAndUsernameStartingWith(Long spaceId, String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query("update MemberLog m set m.muteUntil = :muteUntil where m.spaceId = :spaceId and m.userId= :userId")
    void mute(@Param("spaceId") Long spaceId,@Param("userId") Long userId, @Param("muteUntil") Instant muteUntil);

    Optional<MemberLog> findBySpaceIdAndUserId(Long spaceId, Long userId);

    @Query("select m.spaceId from MemberLog m where m.userId = :userId and m.spaceId in :spaces")
    Set<Long> findMember(@Param("userId") Long xUserId, @Param("spaces") List<Long> spaces);
}
