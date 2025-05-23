package com.heslin.postopia.service.space;

import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.dto.user.UserSummary;
import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.elasticsearch.dto.SearchedSpaceInfo;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public interface SpaceService {

    ResMessage joinSpace(Long spaceId, User user);
    
    ResMessage leaveSpace(Long spaceId, User user);

    Pair<ResMessage, Long> createSpace(User user, String name, String description, MultipartFile avatar);

    Page<SpaceInfo> getSpacesByUserId(Long userId, Pageable pageable);

    Page<SpaceInfo> getPopularSpaces(PopularSpaceOrder order, Pageable pageable);

    SpaceInfo getSpaceInfo(Long spaceId);

    List<Avatar> getSpaceAvatars(List<String> names);

    List<SearchedSpaceInfo> getSearchedSpaceInfos(List<String> names);

    void updateSpace(String spaceName, String description, String avatar);

    void notifyUsers(String spaceName, String s, String spaceMessage);

    void expelUser(Long spaceId, String spaceName, String username);

    void muteUser(String spaceName, String username);

    Instant getForbidden(Long spaceId, String username);

    Page<UserSummary> searchUserByPrefix(String spaceName, String prefix, Pageable pageable);
}
