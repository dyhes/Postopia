package com.heslin.postopia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.SpaceUserInfo;

@Repository
public interface  SpaceUserInfoRepository extends CrudRepository<SpaceUserInfo, Long> {
    long countBySpaceIdAndUserId(Long spaceId, Long userId);
    boolean deleteBySpaceIdAndUserId(Long spaceId, Long userId);
}
