package com.heslin.postopia.service.user;

import com.heslin.postopia.model.User;

public interface UserService {
    public User findUserById(Long id);

    public void updateUserNickName(Long id, String nickname);
}
