package com.heslin.postopia.service.space;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.SpaceInfo;
import com.heslin.postopia.enums.PopularSpaceOrder;
import com.heslin.postopia.model.User;
import com.heslin.postopia.util.Pair;

public interface SpaceService {

    public Message joinSpace(Long spaceId, User user);
    
    public Message leaveSpace(Long spaceId, User user);

    public Pair<Message, Long> createSpace(User user, String name, String description, String avatar);

    public Page<SpaceInfo> getSpacesByUserId(Long userId, Pageable pageable);

    public Page<SpaceInfo> getPopularSpaces(PopularSpaceOrder order, Pageable pageable);
}
