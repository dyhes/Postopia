package com.heslin.postopia.service.user;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.UserInfo;
import com.heslin.postopia.model.User;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    public User findUserById(Long id);

    public void updateUserNickName(Long id, String nickname);

    public void updateUserEmail(String email, User user) throws MessagingException;

    public Message verifyUserEmail(String email, String code, User user);

    String updateUserAvatar(Long id, MultipartFile avatar) throws IOException;

    UserInfo getUserInfo(Long maskId);

    void updateShowEmail(boolean show, Long id);
}
