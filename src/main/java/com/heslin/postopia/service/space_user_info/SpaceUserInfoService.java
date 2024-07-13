package com.heslin.postopia.service.space_user_info;

import com.heslin.postopia.model.SpaceUserInfo;

public interface SpaceUserInfoService {
    boolean isSpaceMember(Long spaceId, Long userId);

    SpaceUserInfo joinSpace(SpaceUserInfo spaceUserInfo);
}
