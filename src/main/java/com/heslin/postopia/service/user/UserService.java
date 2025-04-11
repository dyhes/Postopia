package com.heslin.postopia.service.user;

import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.jpa.model.User;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User findUserById(Long id);

    void updateUserNickName(User user, String nickname);

    void updateUserEmail(String email, User user) throws MessagingException;

    ResMessage verifyUserEmail(String email, String code, User user);

    String updateUserAvatar(UserId id, MultipartFile avatar) throws IOException;

    UserInfo getUserInfo(Long id);

    void updateShowEmail(boolean show, Long id);

    String uploadFile(UserId userId, MultipartFile img, boolean isVideo) throws IOException;

    List<Avatar> getUserAvatars(List<String> names);
}
