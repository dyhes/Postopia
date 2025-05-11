package com.heslin.postopia.service.space;

import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.user.UserSummary;
import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.elasticsearch.dto.SearchedSpaceInfo;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.jpa.model.*;
import com.heslin.postopia.jpa.repository.ForbiddenRepository;
import com.heslin.postopia.jpa.repository.SpaceRepository;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.service.message.MessageService;
import com.heslin.postopia.service.os.OSService;
import com.heslin.postopia.service.space_user_info.SpaceUserInfoService;
import com.heslin.postopia.util.Pair;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpaceServiceImpl implements SpaceService {
    private final SpaceRepository spaceRepository;
    private final SpaceUserInfoService spaceUserInfoService;
    private final OSService osService;
    private final String defaultSpaceAvatar;
    private final KafkaService kafkaService;
    private final MessageService messageService;
    private final ForbiddenRepository forbiddenRepository;

    @Autowired
    public SpaceServiceImpl(@Value("${postopia.avatar.space}") String defaultSpaceAvatar, OSService osService, SpaceRepository spaceRepository, SpaceUserInfoService spaceUserInfoService, KafkaService kafkaService, MessageService messageService, ForbiddenRepository forbiddenRepository) {
        this.osService = osService;
        this.spaceRepository = spaceRepository;
        this.spaceUserInfoService = spaceUserInfoService;
        this.defaultSpaceAvatar = defaultSpaceAvatar;
        this.kafkaService = kafkaService;
        this.messageService = messageService;
        this.forbiddenRepository = forbiddenRepository;
    }

    @Override
    public Page<UserSummary> searchUserByPrefix(String spaceName, String prefix, Pageable pageable) {
        return spaceUserInfoService.searchByPrefix(spaceName, prefix, pageable);
    }

    @Override
    @Transactional
    public void updateSpace(String spaceName, String description, String avatar) {
        spaceRepository.updateSpaceInfo(spaceName, description, avatar);
        Map<String, Object> mp = new HashMap<>();
        mp.put("description", description);
        kafkaService.sendToDocUpdate("space", spaceName, spaceName, mp);
    }

    @Override
    public Instant getForbidden(Long spaceId, String username) {
        return forbiddenRepository.findLog(spaceId, username);
    }

    @Override
    public void notifyUsers(String spaceName, String s, String spaceMessage) {
        List<String> users = spaceUserInfoService.findUsername(spaceName);
        List<Message> messages = users.stream().map(u -> new Message(u, spaceMessage)).toList();
        messageService.batchSave(messages);
    }

    @Override
    @Transactional
    public ResMessage joinSpace(Long spaceId, User user) {
        Space space = spaceRepository.findById(spaceId).orElse(null);
        if (space == null) {
            return new ResMessage("空间不存在", false);
        }

        return joinSpace(space, user);
    }

    @Override
    public void expelUser(Long spaceId, String spaceName, String username) {
        boolean success = spaceUserInfoService.deleteBySpaceNameAndUserName(spaceName, username);
        if (success) {
            kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        }
        Forbidden forbidden = Forbidden.builder().spaceId(spaceId).username(username).build();
        forbiddenRepository.save(forbidden);
    }

    @Override
    public void muteUser(String spaceName, String username) {
        spaceUserInfoService.muteUser(spaceName, username);
    }

    @Override
    public ResMessage leaveSpace(Long spaceId, User user) {
        boolean success = spaceUserInfoService.deleteBySpaceIdAndUserId(spaceId, user.getId());
        if (success) {
            kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        }
        return new ResMessage(success ? "退出成功" : "退出失败, 尚未加入空间", success);
    }

    @Transactional
    public ResMessage joinSpace(Space space, User user) {
        if (spaceUserInfoService.isSpaceMember(space.getId(), user.getId())) {
            return new ResMessage("已经加入过该空间", false);
        }

        SpaceUserInfo spaceUserInfo = SpaceUserInfo.builder().space(space).user(user).spaceName(space.getName()).username(user.getUsername()).lastActiveAt(LocalDate.now()).build();
        spaceUserInfoService.joinSpace(spaceUserInfo);
        kafkaService.sendToSpace(space.getId(), SpaceOperation.MEMBER_JOINED);
        return new ResMessage("加入成功", true);
    }

    @Override
    @Transactional
    public Pair<ResMessage, Long> createSpace(User user, String name, String description, MultipartFile avatar) {
        String avatarUrl = defaultSpaceAvatar;
        if (avatar != null) {
            try {
                avatarUrl = osService.updateSpaceAvatar(name, avatar);
            } catch (IOException e) {
                return new Pair<>(new ResMessage(e.getMessage(), false), null);
            }
        }
        Space space = new Space();
        space.setName(name);
        space.setDescription(description);
        space.setAvatar(avatarUrl);
        space.setMemberCount(0);
        space.setPostCount(0);
        try {
            space = spaceRepository.save(space);
        } catch (DataIntegrityViolationException exception) {
            return new Pair<>(new ResMessage("空间名称已存在", false), null);
        }
        space = spaceRepository.save(space);
        ResMessage resMessage = joinSpace(space, user);
        if (!resMessage.success()) {
            throw new RuntimeException(resMessage.message());
        }
        kafkaService.sendToDocCreate("space", space.getName(), new SpaceDoc(space.getName(),space.getName(),space.getDescription()));
        return new Pair<>(new ResMessage("创建成功", true), space.getId());
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

    @Override
    public SpaceInfo getSpaceInfo(Long spaceId) {
        return spaceRepository.findSpaceInfoById(spaceId).orElse(null);
    }

    @Override
    public List<Avatar> getSpaceAvatars(List<String> names) {
        return spaceRepository.findSpaceAvatars(names);
    }

    @Override
    public List<SearchedSpaceInfo> getSearchedSpaceInfos(List<String> names) {
        return spaceRepository.findSearchedSpaceInfos(names);
    }
}
