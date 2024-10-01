package com.heslin.postopia.service.signup;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupServiceImpl implements SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultUserAvatar;

    @Autowired
    public SignupServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${postopia.avatar.user}") String defaultUserAvatar) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUserAvatar = defaultUserAvatar;
    }

    @Override
    public Message signup(String username, String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setNickname(username);
            user.setAvatar(defaultUserAvatar);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return new Message("用户 @" + username + " 注册成功", true);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException: " + e);
            return new Message("用户 @" + username + " 已存在", false);
        }
    }
}