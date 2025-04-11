package com.heslin.postopia.service.user;

import com.heslin.postopia.elasticsearch.dto.Avatar;
import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.user.UserId;
import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.UserRepository;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.service.mail.MailService;
import com.heslin.postopia.service.os.OSService;
import com.heslin.postopia.service.redis.RedisService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final MailService mailService;

    private final RedisService redisService;

    private final OSService osService;
    private final KafkaService kafkaService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MailService mailService, RedisService redisService, OSService osService, KafkaService kafkaService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.redisService = redisService;
        this.osService = osService;
        this.kafkaService = kafkaService;
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Override
    public void updateUserNickName(User user, String nickname) {
        userRepository.updateNickname(user.getId(), nickname);
        Map<String, Object> mp = new HashMap<>();
        mp.put("nickname", nickname);
        kafkaService.sendToDocUpdate("user", user.getUsername(), user.getUsername(), mp);
    }

    @Override
    public void updateUserEmail(String email, User user) throws MessagingException {
        mailService.sendAuthenticationCode(email, findUserById(user.getId()));
    }

    @Override
    public ResMessage verifyUserEmail(String email, String code, User user) {
        String credential = redisService.get(email);
        if (credential != null) {
            String[] credentials = credential.split("\\.");
            if (user.getId().equals(Long.parseLong(credentials[0])) && code.equals(credentials[1])) {
                redisService.delete(email);
                userRepository.updateEmail(user.getId(), email);
                return new ResMessage("success", true);
            }
        }
        return new ResMessage("doesn't exist or expired", false);

    }

    @Override
    public String updateUserAvatar(UserId id, MultipartFile avatar) throws IOException {
        String url = osService.updateUserAvatar(id, avatar);
        userRepository.updateAvatar(id.getId(), url);
        return url;
    }

    @Override
    public String uploadFile(UserId userId, MultipartFile img, boolean isVideo) throws IOException {
        return osService.uploadFile(userId.toString(), img.getOriginalFilename(), img, isVideo);
    }

    @Override
    public UserInfo getUserInfo(Long id) {
        return userRepository.findUserInfoById(id);
    }

    @Override
    public void updateShowEmail(boolean show, Long id) {
        userRepository.updateShowStatusById(show, id);
    }

    @Override
    public List<Avatar> getUserAvatars(List<String> names) {
        System.out.println("names");
        System.out.println(names);
        var ret =  userRepository.findUserAvatars(names);
        System.out.println("ret");
        System.out.println(ret);
        return ret;
    }
}
