package com.heslin.postopia.service.space_user_info;

import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.user.UserSummary;
import com.heslin.postopia.jpa.model.SpaceUserInfo;
import com.heslin.postopia.jpa.repository.SpaceUserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SpaceUserInfoServiceImpl implements SpaceUserInfoService {

    @Autowired
    private SpaceUserInfoRepository spaceUserInfoRepository;

    @Override
    public Page<UserSummary> searchByPrefix(String spaceName, String prefix, Pageable pageable) {
        return spaceUserInfoRepository.search(spaceName, prefix, pageable);
    }

    @Override
    public Instant getMutedUntil(String spaceName, String username) {
        return spaceUserInfoRepository.getMutedUntil(spaceName, username);
    }

    @Override
    public boolean isSpaceMember(Long spaceId, Long userId) {
        return spaceUserInfoRepository.countBySpaceIdAndUserId(spaceId, userId) > 0;
    }

    @Override
    public List<String> findUsername(String spaceName) {
        return spaceUserInfoRepository.findUsernameBySpaceName(spaceName);
    }

    @Override
    public SpaceUserInfo joinSpace(SpaceUserInfo spaceUserInfo) {
        return spaceUserInfoRepository.save(spaceUserInfo);
    }

    @Override
    public boolean deleteBySpaceIdAndUserId(Long spaceId, Long userId) {
        return spaceUserInfoRepository.deleteBySpaceIdAndUserId(spaceId, userId) == 1;
    }

    @Override
    public boolean deleteBySpaceNameAndUserName(String spaceName, String username) {
        return spaceUserInfoRepository.deleteBySpaceNameAndUserName(spaceName, username) == 1;
    }

    @Override
    public void muteUser(String spaceName, String username) {
        Instant muteUntil = Instant.now().plus(7, ChronoUnit.DAYS);
        spaceUserInfoRepository.muteUser(spaceName, username, muteUntil);
    }
}
