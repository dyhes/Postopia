package com.heslin.postopia.space.repository;

import com.heslin.postopia.space.dto.SearchSpaceInfo;
import com.heslin.postopia.space.dto.SpaceAvatar;
import com.heslin.postopia.space.dto.SpaceInfo;
import com.heslin.postopia.space.dto.VoteSpaceInfo;
import com.heslin.postopia.space.model.Space;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    @Query("select new com.heslin.postopia.space.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.postCount, s.memberCount) from Space s order by (2 *s.memberCount + 0.5 * s.postCount) desc")
    Page<SpaceInfo> findSpaceInfosByPopularity(Pageable pageable);

    @Query("select new com.heslin.postopia.space.dto.SpaceInfo(s.id, s.name, s.avatar, s.description, s.createdAt, s.postCount, s.memberCount) from Space s where s.id in (select m.spaceId from MemberLog m where m.userId = ?1) order by s.createdAt asc ")
    Page<SpaceInfo> findSpaceInfosByUserId(Long queryId, Pageable pageable);

    SpaceInfo findSpaceInfoById(Long id);

    List<SpaceAvatar> findSpaceAvatarsByIdIn(List<Long> ids);

    List<SearchSpaceInfo> findSearchSpaceInfosByIdIn(List<Long> ids);

    @Transactional
    @Modifying
    @Query("update Space s set s.avatar = ?3, s.description = ?2 where s.id = ?1")
    void updateInfo(Long spaceId, String description, String avatar);

    VoteSpaceInfo findVoteSpaceInfoById(Long spaceId);
}
