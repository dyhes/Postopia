package com.heslin.postopia.service.space_user_info;

import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.user.UserSummary;
import com.heslin.postopia.jpa.model.SpaceUserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface SpaceUserInfoService {
    boolean isSpaceMember(Long spaceId, Long userId);

    SpaceUserInfo joinSpace(SpaceUserInfo spaceUserInfo);

    boolean deleteBySpaceIdAndUserId(Long spaceId, Long userId);

    boolean deleteBySpaceNameAndUserName(String spaceName, String username);

    List<String> findUsername(String spaceName);

    void muteUser(String spaceName, String username);
    
    Instant getMutedUntil(String spaceName, String username);

    Page<UserSummary> searchByPrefix(String spaceName, String prefix, Pageable pageable);
}
