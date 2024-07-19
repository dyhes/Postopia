package com.heslin.postopia.service.space;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.SpaceUserInfo;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.SpaceRepository;
import com.heslin.postopia.service.space_user_info.SpaceUserInfoService;
import com.heslin.postopia.util.Pair;

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
        spaceUserInfo.setLastActiveAt(LocalDate.now());
        spaceUserInfoService.joinSpace(spaceUserInfo);
        return new Message("加入成功", true);
    }

    @Override
    public Message leaveSpace(Long spaceId, User user) {
        boolean success = spaceUserInfoService.deleteBySpaceIdAndUserId(spaceId, user.getId());
        return new Message(success ? "退出成功" : "退出失败, 尚未加入空间", success);
    }

    @Override
    @Transactional
    public Pair<Message, Long> createSpace(User user, String name, String description, String avatar) {
        Space existingSpace = spaceRepository.findByName(name);
        if (existingSpace != null) {
            return new Pair<>(new Message("空间名已存在", false), existingSpace.getId());
        }

        Space space = new Space();
        space.setName(name);
        space.setDescription(description);
        space.setAvatar(avatar);
        space = spaceRepository.save(space);
        Message message = joinSpace(space, user);
        if (!message.success()) {
            return new Pair<>(new Message("加入空间失败, 请重试", false), Long.MIN_VALUE);
        }

        return new Pair<>(new Message("创建成功", true), space.getId());
    }

    @Override
    public Page<SpaceInfo> getSpacesByUserId(Long userId, Pageable pageable) {
        return spaceRepository.findSpaceInfosByUserId(userId, pageable);
    }

    @Override
    public Page<SpaceInfo> getPopularSpaces(PopularSpaceOrder order, Pageable pageable) {
        switch (order) {
            case MEMBERCOUNT -> {
                return spaceRepository.findPopularSpacesByMemberCount(pageable);
            }
            case POSTCOUNT -> {
                return spaceRepository.findPopularSpacesByPostCount(pageable);
            }
            default -> throw new UnsupportedOperationException("Unimplemented order " + order);
        }
    }
    
}
