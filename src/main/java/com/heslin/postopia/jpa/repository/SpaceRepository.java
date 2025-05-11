package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.elasticsearch.dto.SearchedSpaceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.jpa.model.Space;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends PagingAndSortingRepository<Space, Long>, JpaRepository<Space, Long> {
    Space findByName(String name);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount, s.postCount) from Space s JOIN s.userInfos sui JOIN sui.user u where u.id=:id")
    Page<SpaceInfo> findSpaceInfosByUserId(@Param("id")Long userId, Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount, s.postCount) from Space s order by s.memberCount desc")
    Page<SpaceInfo> findPopularSpacesByMemberCount(Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.memberCount, s.postCount) from Space s JOIN s.posts p GROUP BY s.id, s.name, s.avatar order by count(p) desc")
    Page<SpaceInfo> findPopularSpacesByPostCount(Pageable pageable);

    Optional<SpaceInfo> findSpaceInfoById(Long spaceId);

    @Query("select new com.heslin.postopia.elasticsearch.dto.Avatar(s.name, s.avatar) from Space s where s.name in :names")
    List<Avatar> findSpaceAvatars(@Param("names") List<String> names);

    @Query("select new com.heslin.postopia.elasticsearch.dto.SearchedSpaceInfo(s.name, s.avatar, s.memberCount, s.postCount) from Space s where s.name in :names")
    List<SearchedSpaceInfo> findSearchedSpaceInfos(@Param("names") List<String> names);

    @Modifying
    @Query("update Space s set s.description = :description, s.avatar = :avatar where s.name = :name")
    void updateSpaceInfo(@Param("name") String name, @Param("description") String description, @Param("avatar") String avatar);
}
