package com.heslin.postopia.space.repository;

import com.heslin.postopia.space.model.MemberLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberLog, Long> {
    @Transactional
    @Modifying
    @Query("delete from MemberLog m where m.spaceId = :spaceId and m.userId = :userId")
    int deleteBySpaceIdAndUserId( @Param("userId") Long userId, @Param("spaceId") Long spaceId);
}
