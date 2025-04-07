package com.heslin.postopia.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.jpa.model.Space;

import java.util.Optional;

@Repository
public interface SpaceRepository extends PagingAndSortingRepository<Space, Long>, JpaRepository<Space, Long> {
    Space findByName(String name);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount) from Space s JOIN s.userInfos sui JOIN sui.user u where u.id=:id")
    Page<SpaceInfo> findSpaceInfosByUserId(@Param("id")Long userId, Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount) from Space s order by s.memberCount desc")
    Page<SpaceInfo> findPopularSpacesByMemberCount(Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount) from Space s JOIN s.posts p GROUP BY s.id, s.name, s.avatar order by count(p) desc")
    Page<SpaceInfo> findPopularSpacesByPostCount(Pageable pageable);

    Optional<SpaceInfo> findSpaceInfoById(Long spaceId);
}
