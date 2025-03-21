package com.heslin.postopia.service.user;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.UserId;
import com.heslin.postopia.dto.UserInfo;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;
import com.heslin.postopia.service.mail.MailService;
import com.heslin.postopia.service.os.OSService;
import com.heslin.postopia.service.redis.RedisService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final MailService mailService;

    private final RedisService redisService;

    private final OSService osService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MailService mailService, RedisService redisService, OSService osService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.redisService = redisService;
        this.osService = osService;
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Override
    public void updateUserNickName(Long id, String nickname) {
        userRepository.updateNickname(id, nickname);
    }

    @Override
    public void updateUserEmail(String email, User user) throws MessagingException {
        mailService.sendAuthenticationCode(email, findUserById(user.getId()));
    }

    @Override
    public Message verifyUserEmail(String email, String code, User user) {
        String credential = redisService.get(email);
        if (credential != null) {
            String[] credentials = credential.split("\\.");
            if (user.getId().equals(Long.parseLong(credentials[0])) && code.equals(credentials[1])) {
                redisService.delete(email);
                userRepository.updateEmail(user.getId(), email);
                return new Message("success", true);
            }
        }
        return new Message("doesn't exist or expired", false);

    }

    @Override
    public String updateUserAvatar(UserId id, MultipartFile avatar) throws IOException {
        String url = osService.updateUserAvatar(id, avatar);
        userRepository.updateAvatar(id.getId(), url);
        return url;
    }

    @Override
    public UserInfo getUserInfo(Long id) {
        return userRepository.findUserInfoById(id);
    }

    @Override
    public void updateShowEmail(boolean show, Long id) {
        userRepository.updateShowStatusById(show, id);
    }
}
