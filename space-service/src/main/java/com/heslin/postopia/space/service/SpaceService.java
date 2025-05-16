package com.heslin.postopia.space.service;

import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ApiResponse;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.SpaceOperation;
import com.heslin.postopia.search.model.SpaceDoc;
import com.heslin.postopia.space.dto.SearchSpaceInfo;
import com.heslin.postopia.space.dto.SpaceAvatar;
import com.heslin.postopia.space.dto.SpaceInfo;
import com.heslin.postopia.space.feign.UserClient;
import com.heslin.postopia.space.model.MemberLog;
import com.heslin.postopia.space.model.Space;
import com.heslin.postopia.space.repository.SpaceRepository;
import com.heslin.postopia.user.dto.SearchUserInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RefreshScope
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final MemberService memberService;
    private final KafkaService kafkaService;
    @Value("${postopia.avatar.space}")
    private String defaultSpaceAvatar;
    private final UserClient userClient;

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
        kafkaService.sendToDocCreate("space", space.getName(), new SpaceDoc(space.getId().toString(), space.getName(),space.getDescription()));
        return space.getId();
    }

    public void joinSpace(String username, Long userId, Long spaceId) {
        memberService.joinSpace(username, userId, spaceId);
    }

    public ResMessage leaveSpace(Long userId, Long spaceId) {
        boolean success = memberService.leaveSpace(userId, spaceId);
        if (success) {
            kafkaService.sendToSpace(spaceId, SpaceOperation.MEMBER_LEFT);
        }
        return new ResMessage(success ? "退出成功" : "退出失败, 尚未加入空间", success);
    }

    public Page<SpaceInfo> getPopularSpaces(Pageable pageable) {
        return spaceRepository.findSpaceInfosByPopularity(pageable);
    }

    public Page<SpaceInfo> getUserSpaces(Long queryId, Pageable pageable) {
        return spaceRepository.findSpaceInfosByUserId(queryId, pageable);
    }

    public SpaceInfo getSpaceInfo(Long spaceId) {
        return spaceRepository.findSpaceInfoById(spaceId);
    }

    public List<SpaceAvatar> getSpaceAvatars(List<Long> ids) {
        return spaceRepository.findSpaceAvatarsByIdIn(ids);
    }

    public List<SearchSpaceInfo> getSearchSpaceInfos(List<Long> ids) {
        return spaceRepository.findSearchSpaceInfosByIdIn(ids);
    }

    public Page<SearchUserInfo> searchMemberByPrefix(Long spaceId, String prefix, Pageable pageable) {
        Page<MemberLog>  localRet = memberService.searchByPrefix(spaceId, prefix, pageable);
        var  remoteRet = userClient.getSpaceUserInfo(localRet.getContent().stream().map(memberLog -> new UserId(memberLog.getUserId())).toList());
        List<SearchUserInfo> infos = Objects.requireNonNull(remoteRet.getBody()).getData();
        System.out.println("infos");
        infos.forEach(System.out::println);
        return new PageImpl<>(infos, pageable, localRet.getTotalElements());
    }

    public ResponseEntity<ApiResponse<String>> uploadAvatar(MultipartFile avatar, Long xUserId) {
        return userClient.uploadAvatar(avatar, false, xUserId);
    }
}
