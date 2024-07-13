package com.heslin.postopia.service.space;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.SpaceUserInfo;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.SpaceRepository;
import com.heslin.postopia.service.space_user_info.SpaceUserInfoService;

import jakarta.transaction.Transactional;

@Service
public class SpaceServiceImpl implements SpaceService {
    @Autowired
    private SpaceRepository spaceRepository;
    @Autowired
    private SpaceUserInfoService spaceUserInfoService;

    @Override
    public Message joinSpace(Long spaceId, User user) {
        Space space = spaceRepository.findById(spaceId).orElse(null);
        if (space == null) {
            return new Message("空间不存在", false);
        }

        return joinSpace(space, user);
    }

    public Message joinSpace(Space space, User user) {
        if (spaceUserInfoService.isSpaceMember(space.getId(), user.getId())) {
            return new Message("已经加入过该空间", false);
        }

        SpaceUserInfo spaceUserInfo = new SpaceUserInfo();
        spaceUserInfo.setSpace(space);
        spaceUserInfo.setUser(user);
        // tbc: add the spaceUserInfo on the other side of relationship?
        spaceUserInfo.setLastActiveAt(LocalDate.now());
        spaceUserInfoService.joinSpace(spaceUserInfo);
        return new Message("加入成功", true);
    }

    @Override
    public Message leaveSpace(Long spaceId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leaveSpace'");
    }

    @Override
    @Transactional
    public Message createSpace(User user, String name, String description, String avatar) {
        Space existingSpace = spaceRepository.findByName(name);
        if (existingSpace != null) {
            return new Message("空间已存在", false);
        }

        Space space = new Space();
        space.setName(name);
        space.setDescription(description);
        space.setAvatar(avatar);
        space = spaceRepository.save(space);
        Message message = joinSpace(space, user);
        if (!message.success()) {
            return new Message("加入空间失败, 请重试", false);
        }

        return new Message("创建成功", true);
    }
    
}
