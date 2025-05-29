package com.heslin.postopia.space.service;

import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.SpaceOperation;
import com.heslin.postopia.search.model.SpaceDoc;
import com.heslin.postopia.space.dto.*;
import com.heslin.postopia.space.feign.UserClient;
import com.heslin.postopia.space.model.MemberLog;
import com.heslin.postopia.space.model.Space;
import com.heslin.postopia.space.repository.SpaceRepository;
import com.heslin.postopia.user.dto.UserInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RefreshScope
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final MemberService memberService;
    private final KafkaService kafkaService;
    @Value("${postopia.avatar.space}")
    private String defaultSpaceAvatar;
    private final UserClient userClient;

    private List<SpaceInfo> completeSpaceInfo(Long xUserId, List<SpacePart> spaceParts) {
        List<Long> spaceIds = spaceParts.stream().map(SpacePart::id).toList();
        Set<Long> ids = memberService.findMember(xUserId, spaceIds);
        return spaceParts.stream().map(spacePart -> ids.contains(spacePart.id())? new SpaceInfo(spacePart, true) : new SpaceInfo(spacePart, false)).toList();
    }

    private List<SearchSpaceInfo> completeSearchSpaceInfo(Long xUserId, List<SearchSpacePart> spaceParts) {
        List<Long> spaceIds = spaceParts.stream().map(SearchSpacePart::id).toList();
        Set<Long> ids = memberService.findMember(xUserId, spaceIds);
        return spaceParts.stream().map(spacePart -> ids.contains(spacePart.id())? new SearchSpaceInfo(spacePart, true) : new SearchSpaceInfo(spacePart, false)).toList();
    }

    @Autowired
    public SpaceService(SpaceRepository spaceRepository, MemberService memberService, KafkaService kafkaService, UserClient userClient) {
        this.spaceRepository = spaceRepository;
        this.memberService = memberService;
        this.kafkaService = kafkaService;
        this.userClient = userClient;
    }

    @Transactional
    public Long createSpace(String username, Long userId, String name, String description, String avatar) {
        Space space = Space.builder().avatar(avatar == null? defaultSpaceAvatar : avatar).description(description).name(name).build();
        try {
            space = spaceRepository.save(space);
        } catch (DataIntegrityViolationException exception) {
            System.out.println(exception.getMessage());
            throw new DataIntegrityViolationException("空间名称已存在");
        }
        joinSpace(username, userId, space.getId());
        String id = space.getId().toString();
        kafkaService.sendToDocCreate("space", id, new SpaceDoc(id, space.getName(),space.getDescription()));
        return space.getId();
    }

    public void joinSpace(String username, Long userId, Long spaceId) {
        memberService.joinSpace(username, userId, spaceId);
        kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_JOINED);
    }

    public ResMessage leaveSpace(Long userId, Long spaceId) {
        boolean success = memberService.leaveSpace(userId, spaceId);
        if (success) {
            kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        }
        return new ResMessage(success ? "退出成功" : "退出失败, 尚未加入空间", success);
    }

    public Page<SpaceInfo> getPopularSpaces(Pageable pageable, Long xUserId) {
        Page<SpacePart> spaceParts = spaceRepository.findSpaceInfosByPopularity(pageable);
        List<SpaceInfo> spaceInfos = completeSpaceInfo(xUserId, spaceParts.getContent());
        return new PageImpl<>(spaceInfos, pageable, spaceParts.getTotalElements());
    }

    public Page<SpacePart> getUserSpaces(Long queryId, Pageable pageable) {
        return spaceRepository.findSpaceInfosByUserId(queryId, pageable);
    }

    public SpaceInfo getSpaceInfo(Long spaceId, Long userId) {
        SpacePart spacePart = spaceRepository.findSpaceInfoById(spaceId);
        List<SpaceInfo> spaceInfos = completeSpaceInfo(userId, List.of(spacePart));
        return spaceInfos.get(0);
    }

    public List<SpaceAvatar> getSpaceAvatars(List<Long> ids) {
        return spaceRepository.findSpaceAvatarsByIdIn(ids);
    }

    public List<SearchSpaceInfo> getSearchSpaceInfos(List<Long> ids, Long xUserId) {
        List<SearchSpacePart> searchSpaceParts = spaceRepository.findSearchSpaceInfosByIdIn(ids);
        return completeSearchSpaceInfo(xUserId, searchSpaceParts);
    }

    public Page<UserInfo> searchMemberByPrefix(Long spaceId, String prefix, Pageable pageable) {
        Page<MemberLog>  localRet = memberService.searchByPrefix(spaceId, prefix, pageable);
        List<Long> userId = localRet.getContent().stream().map(MemberLog::getUserId).toList();
        System.out.println("userId");
        userId.forEach(System.out::println);
        List<UserInfo> infos = userClient.getSpaceUserInfo(userId);
        return new PageImpl<>(infos, pageable, localRet.getTotalElements());
    }

    public String uploadAvatar(MultipartFile avatar, Long xUserId) {
        return userClient.uploadAvatar(avatar, xUserId);
    }


    private void notifyMember(String message) {
        //not impl
    }

    public void expelUser(Long spaceId, Long userId, String reason) {
        boolean success = memberService.leaveSpace(userId, spaceId);
        if (success) {
            kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        }
        memberService.forbid(spaceId, userId);
        notifyMember(reason);
    }

    public void muteUser(Long spaceId, Long userId, String reason) {
        memberService.mute(spaceId, userId);
        notifyMember(reason);
    }


    public void updateInfo(Long spaceId, String description, String avatar) {
        spaceRepository.updateInfo(spaceId, description, avatar);
        Map<String, Object> mp = new HashMap<>();
        mp.put("description", description);
        String id = spaceId.toString();
        kafkaService.sendToDocUpdate("space", id, id, mp);
    }

    public boolean isEligible(Long spaceId, Long userId) {
        try {
            return memberService.findBySpaceIdAndUserId(spaceId, userId).isEligible();
        } catch (RuntimeException e) {
            System.out.println("is eligible error");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public VoteSpaceInfo findVoteSpaceInfo(Long spaceId) {
        return spaceRepository.findVoteSpaceInfoById(spaceId);
    }
}
