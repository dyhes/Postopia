package com.heslin.postopia.service.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Credential;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.UserRepository;
import com.heslin.postopia.service.jwt.JWTService;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTService jwtService;

    @Override
    public Credential login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户 @" + username + "不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户 @" + username + "密码错误");
        }
        String refreshToken = jwtService.generateRefreshToken(user);
        String accessToken = jwtService.generateToken(user);
        return new Credential(refreshToken, accessToken);
    }
}
