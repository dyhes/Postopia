package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.jpa.model.SpaceUserInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface  SpaceUserInfoRepository extends JpaRepository<SpaceUserInfo, Long> {
    long countBySpaceIdAndUserId(Long spaceId, Long userId);

    @Transactional
    @Modifying
    @Query("delete from SpaceUserInfo sui where sui.space.id = :spaceId and sui.user.id = :userId")
    int deleteBySpaceIdAndUserId(@Param("spaceId") Long spaceId, @Param("userId") Long userId);
}
