package com.heslin.postopia.service.space;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.User;

public interface SpaceService {

    public Message joinSpace(Long spaceId, User user);
    
    public Message leaveSpace(Long spaceId, Long userId);

    public Message createSpace(User user, String name, String description, String avatar);
}
