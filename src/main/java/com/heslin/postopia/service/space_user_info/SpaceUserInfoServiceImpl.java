package com.heslin.postopia.service.space_user_info;

import com.heslin.postopia.jpa.model.SpaceUserInfo;
import com.heslin.postopia.jpa.repository.SpaceUserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpaceUserInfoServiceImpl implements SpaceUserInfoService {

    @Autowired
    private SpaceUserInfoRepository spaceUserInfoRepository;

    @Override
    public boolean isSpaceMember(Long spaceId, Long userId) {
        return spaceUserInfoRepository.countBySpaceIdAndUserId(spaceId, userId) > 0;
    }

    @Override
    public SpaceUserInfo joinSpace(SpaceUserInfo spaceUserInfo) {
        return spaceUserInfoRepository.save(spaceUserInfo);
    }

    @Override
    public boolean deleteBySpaceIdAndUserId(Long spaceId, Long userId) {
        return spaceUserInfoRepository.deleteBySpaceIdAndUserId(spaceId, userId) == 1;
    }
    
}
