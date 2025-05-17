package com.heslin.postopia.user.service;

import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.jwt.JWTService;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.redis.RedisService;
import com.heslin.postopia.search.model.UserDoc;
import com.heslin.postopia.user.Repository.UserRepository;
import com.heslin.postopia.user.dto.Credential;
import com.heslin.postopia.user.dto.SearchUserInfo;
import com.heslin.postopia.user.dto.UserAvatar;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.model.User;
import com.heslin.postopia.user.request.RefreshRequest;
import com.heslin.postopia.user.request.SignInRequest;
import com.heslin.postopia.user.request.SignUpRequest;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final OStorageService oStorageService;
    private final MailService mailService;
    private final RedisService redisService;
    private final KafkaService kafkaService;
    @Value("${postopia.avatar.user}")
    private String defaultUserAvatar;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService, OStorageService oStorageService, MailService mailService, RedisService redisService, KafkaService kafkaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.oStorageService = oStorageService;
        this.mailService = mailService;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
    }

    public ResMessage signup(SignUpRequest signupRequest) {
        String username = signupRequest.username();
        String password = signupRequest.password();
        try {
            User user = User.builder().username(username).nickname(username).avatar(defaultUserAvatar)
            .postCount(0L)
            .commentCount(0L)
            .credit(0L)
            .password(passwordEncoder.encode(password))
            .showEmail(false)
            .build();
            user = userRepository.save(user);
            String id = UserId.encode(user.getUserId());
            kafkaService.sendToDocCreate("user", id, new UserDoc(id, user.getUsername(), user.getNickname()));
            return new ResMessage("用户 @" + username + " 注册成功", true);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException: " + e);
            return new ResMessage("用户 @" + username + " 已存在", false);
        }
    }

    public Credential signin(SignInRequest signInRequest) {
        String username = signInRequest.username();
        String password = signInRequest.password();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户 @" + username + "不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户 @" + username + "密码错误");
        }
        String refreshToken = jwtService.generateRefreshToken(user.getUserId(), username);
        String accessToken = jwtService.refresh(refreshToken);
        return new Credential(refreshToken, accessToken);
    }

    public String refresh(RefreshRequest refreshRequest) {
        return jwtService.refresh(refreshRequest.refreshToken());
    }


    public UserInfo getUserInfo(Long userId) {
        return userRepository.findUserInfoByUserId(userId);
    }

    public void updateUserNickName(Long userId, String username, String nickname) {
        userRepository.updateNickname(userId, nickname);
        Map<String, Object> mp = new HashMap<>();
        mp.put("nickname", nickname);
        kafkaService.sendToDocUpdate("user", UserId.encode(userId), username, mp);
    }

    public void updateUserIntroduction(Long userId, String introduction) {
        userRepository.updateIntroduction(userId, introduction);
    }

    public List<UserAvatar> getUserAvatars(List<Long> ids) {
        return userRepository.findAvatarsByUserIdIn(ids);
    }

    public List<SearchUserInfo> getSearchUserInfos(List<Long> ids) {
        return userRepository.findSearchUserInfosByUserIdIn(ids);
    }

    public String uploadAsset(UserId userId, MultipartFile file, boolean isVideo) throws IOException {
        return oStorageService.uploadAsset(userId.toString(), file.getOriginalFilename(), file, isVideo);
    }

    public void updateUserAvatar(Long userId, String url) {
        userRepository.updateAvatar(userId, url);
    }

    public void updateUserShowEmail(Long userId, boolean show) {
        userRepository.updateShowStatus(show, userId);
    }

    public void updateUserEmail(Long userId, String username, String email) throws MessagingException {
        mailService.sendAuthCode(userId, username, email);
    }


    public ResMessage verifyUserEmail(Long userId, String email, String authCode) {
        String credential = redisService.get(authCode);
        if (credential != null) {
            String[] credentials = credential.split(";");
            if (userId.equals(Long.parseLong(credentials[0])) && email.equals(credentials[1])) {
                redisService.delete(email);
                userRepository.updateEmail(userId, email);
                return new ResMessage("邮箱绑定成功", true);
            }
        }
        return new ResMessage("验证码不存在或已失效", false);
    }
}
