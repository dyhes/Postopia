package com.heslin.postopia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.model.Space;

@Repository
public interface SpaceRepository extends PagingAndSortingRepository<Space, Long>, CrudRepository<Space, Long> {
    Space findByName(String name);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar) from Space s JOIN s.userInfos sui JOIN sui.user u where u.id=:id")
    Page<SpaceInfo> findSpaceInfosByUserId(@Param("id")Long userId, Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar) from Space s JOIN s.userInfos sui GROUP BY s.id, s.name, s.avatar order by count(sui) desc")
    Page<SpaceInfo> findPopularSpacesByMemberCount(Pageable pageable);

    @Query("select new com.heslin.postopia.dto.SpaceInfo(s.id, s.name, s.avatar) from Space s JOIN s.posts p GROUP BY s.id, s.name, s.avatar order by count(p) desc")
    Page<SpaceInfo> findPopularSpacesByPostCount(Pageable pageable);

}
