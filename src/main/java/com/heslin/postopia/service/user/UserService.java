package com.heslin.postopia.service.user;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.User;
import jakarta.mail.MessagingException;

public interface UserService {
    public User findUserById(Long id);

    public void updateUserNickName(Long id, String nickname);

    public void updateUserEmail(String email, User user) throws MessagingException;

    public Message verifyUserEmail(String email, String code, User user);
}
