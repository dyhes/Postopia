package com.heslin.postopia.service.space_user_info;

import com.heslin.postopia.jpa.model.SpaceUserInfo;

import java.util.List;

public interface SpaceUserInfoService {
    boolean isSpaceMember(Long spaceId, Long userId);

    SpaceUserInfo joinSpace(SpaceUserInfo spaceUserInfo);

    boolean deleteBySpaceIdAndUserId(Long spaceId, Long userId);

    List<String> findUsername(String spaceName);
}
