package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.user.UserSummary;
import com.heslin.postopia.jpa.model.SpaceUserInfo;
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

@Repository
public interface  SpaceUserInfoRepository extends JpaRepository<SpaceUserInfo, Long> {
    long countBySpaceIdAndUserId(Long spaceId, Long userId);

    @Transactional
    @Modifying
    @Query("delete from SpaceUserInfo sui where sui.space.id = :spaceId and sui.user.id = :userId")
    int deleteBySpaceIdAndUserId(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

    @Query("select sui.username from SpaceUserInfo sui where sui.space.name = :spaceName")
    List<String> findUsernameBySpaceName(@Param("spaceName") String spaceName);

    @Transactional
    @Modifying
    @Query("delete from SpaceUserInfo sui where sui.spaceName = :spaceName and sui.username= :username")
    int deleteBySpaceNameAndUserName(@Param("spaceName") String spaceName,@Param("username") String username);


    @Transactional
    @Modifying
    @Query("update SpaceUserInfo sui set sui.muteUntil = :muteUntil where sui.spaceName = :spaceName and sui.username= :username")
    void muteUser(@Param("spaceName") String spaceName,@Param("username") String username, @Param("muteUntil") Instant muteUntil);

    @Query("select sui.muteUntil from SpaceUserInfo sui where sui.spaceName = :spaceName and sui.username= :username")
    Instant getMutedUntil(@Param("spaceName") String spaceName,@Param("username") String username);

    @Query("select new com.heslin.postopia.dto.user.UserSummary(u.username, u.nickname, u.avatar) from SpaceUserInfo sui join sui.user u where sui.spaceName = :spaceName and sui.username like concat(:prefix, '%')")
    Page<UserSummary> search(@Param("spaceName")String spaceName, @Param("prefix") String prefix, Pageable pageable);
}
