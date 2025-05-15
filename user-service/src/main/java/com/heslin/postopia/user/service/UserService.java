package com.heslin.postopia.user.service;

import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.common.jwt.JWTService;
import com.heslin.postopia.user.Repository.UserRepository;
import com.heslin.postopia.user.dto.Credential;
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

@Service
@RefreshScope
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    @Value("${postopia.avatar.user}")
    private String defaultUserAvatar;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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


}
