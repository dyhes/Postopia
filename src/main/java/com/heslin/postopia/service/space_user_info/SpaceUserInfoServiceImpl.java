package com.heslin.postopia.service.space_user_info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heslin.postopia.model.SpaceUserInfo;
import com.heslin.postopia.repository.SpaceUserInfoRepository;

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
    
}
