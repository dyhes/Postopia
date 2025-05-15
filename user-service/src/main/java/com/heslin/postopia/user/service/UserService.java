package com.heslin.postopia.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.common.dto.Avatar;
import com.heslin.postopia.common.dto.SearchUserInfo;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.jwt.JWTService;
import com.heslin.postopia.user.Repository.UserRepository;
import com.heslin.postopia.user.dto.Credential;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.model.User;
import com.heslin.postopia.user.request.RefreshRequest;
import com.heslin.postopia.user.request.SignInRequest;
import com.heslin.postopia.user.request.SignUpRequest;
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
    @Value("${postopia.avatar.user}")
    private String defaultUserAvatar;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService, OStorageService oStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.oStorageService = oStorageService;
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
            userRepository.save(user);
            //kafkaService.sendToDocCreate("user", user.getUsername(), new UserDoc(user.getUsername(), user.getUsername(), user.getNickname()));
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
        String refreshToken = jwtService.generateRefreshToken(user.getId(), username);
        String accessToken = jwtService.refresh(refreshToken);
        return new Credential(refreshToken, accessToken);
    }

    public String refresh(RefreshRequest refreshRequest) {
        return jwtService.refresh(refreshRequest.refreshToken());
    }


    public UserInfo getUserInfo(String username) {
        return userRepository.findUserInfoByUsername(username);
    }

    public void updateUserNickName(Long userId, String nickname) {
        userRepository.updateNickname(userId, nickname);
//        Map<String, Object> mp = new HashMap<>();
//        mp.put("nickname", nickname);
//        kafkaService.sendToDocUpdate("user", user.getUsername(), user.getUsername(), mp);
    }

    public void updateUserIntroduction(Long userId, String introduction) {
        userRepository.updateIntroduction(userId, introduction);
    }

    public List<Avatar> getUserAvatars(List<String> names) {
        return userRepository.findAvatarsByUsernameIn(names);
    }

    public List<SearchUserInfo> getSearchUserInfos(List<String> names) {
        return userRepository.findSearchUserInfosByUsernameIn(names);
    }

    public String uploadAsset(UserId userId, MultipartFile file, boolean isVideo) throws IOException {
        return oStorageService.uploadAsset(userId.toString(), file.getOriginalFilename(), file, isVideo);
    }

    public void updateUserAvatar(Long userId, String url) {
        userRepository.updateAvatar(userId, url);
    }
}
