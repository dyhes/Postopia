package com.heslin.postopia.service.user;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;
import com.heslin.postopia.service.mail.MailService;
import com.heslin.postopia.service.redis.RedisService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisService redisService;

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
            System.out.println(credential);
            String[] credentials = credential.split("\\.");
            System.out.println(credentials);
            if (user.getId().equals(Long.parseLong(credentials[0])) && code.equals(credentials[1])) {
                redisService.delete(email);
                userRepository.updateEmail(user.getId(), email);
                return new Message("success", true);
            }
        }
        return new Message("doesn't exist or expired", false);

    }

}
