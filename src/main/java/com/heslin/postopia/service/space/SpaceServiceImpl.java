package com.heslin.postopia.service.space;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.jpa.model.Space;
import com.heslin.postopia.jpa.model.SpaceUserInfo;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.SpaceRepository;
import com.heslin.postopia.kafka.KafkaService;
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
import java.time.LocalDate;

@Service
public class SpaceServiceImpl implements SpaceService {
    private final SpaceRepository spaceRepository;
    private final SpaceUserInfoService spaceUserInfoService;
    private final OSService osService;
    private final String defaultSpaceAvatar;
    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SpaceServiceImpl(@Value("${postopia.avatar.space}") String defaultSpaceAvatar, OSService osService, SpaceRepository spaceRepository, SpaceUserInfoService spaceUserInfoService, KafkaService kafkaService, ObjectMapper objectMapper) {
        this.osService = osService;
        this.spaceRepository = spaceRepository;
        this.spaceUserInfoService = spaceUserInfoService;
        this.defaultSpaceAvatar = defaultSpaceAvatar;
        this.kafkaService = kafkaService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Message joinSpace(Long spaceId, User user) {
        Space space = spaceRepository.findById(spaceId).orElse(null);
        if (space == null) {
            return new Message("空间不存在", false);
        }

        return joinSpace(space, user);
    }

    @Override
    public Message leaveSpace(Long spaceId, User user) {
        boolean success = spaceUserInfoService.deleteBySpaceIdAndUserId(spaceId, user.getId());
        kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        return new Message(success ? "退出成功" : "退出失败, 尚未加入空间", success);
    }

    @Transactional
    public Message joinSpace(Space space, User user) {
        if (spaceUserInfoService.isSpaceMember(space.getId(), user.getId())) {
            return new Message("已经加入过该空间", false);
        }

        SpaceUserInfo spaceUserInfo = new SpaceUserInfo();
        spaceUserInfo.setSpace(space);
        spaceUserInfo.setUser(user);
        spaceUserInfo.setLastActiveAt(LocalDate.now());
        spaceUserInfoService.joinSpace(spaceUserInfo);
        kafkaService.sendToSpace(space.getId(), SpaceOperation.MEMBER_JOINED);
        return new Message("加入成功", true);
    }

    @Override
    @Transactional
    public Pair<Message, Long> createSpace(User user, String name, String description, MultipartFile avatar) {
        String avatarUrl = defaultSpaceAvatar;
        if (avatar != null) {
            try {
                avatarUrl = osService.updateSpaceAvatar(name, avatar);
            } catch (IOException e) {
                return new Pair<>(new Message(e.getMessage(), false), null);
            }
        }
        Space space = new Space();
        space.setName(name);
        space.setDescription(description);
        space.setAvatar(avatarUrl);
        space.setMemberCount(0);
        try {
            space = spaceRepository.save(space);
        } catch (DataIntegrityViolationException exception) {
            return new Pair<>(new Message("空间名称已存在", false), null);
        }
        space = spaceRepository.save(space);
        Message message = joinSpace(space, user);
        if (!message.success()) {
            throw new RuntimeException(message.message());
        }
        try {
            kafkaService.sendToSpaceCreate(space.getId(), objectMapper.writeValueAsString(new SpaceDoc(space.getName(),space.getName(),space.getDescription(),space.getAvatar(), 1)));
        } catch (JsonProcessingException e) {
            System.out.println("Kafka send error: " + e.getMessage());
            throw new RuntimeException(e);
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
