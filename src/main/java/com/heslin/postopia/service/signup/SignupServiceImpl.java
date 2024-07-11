package com.heslin.postopia.service.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;

@Service
public class SignupServiceImpl implements SignupService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Message signup(String username, String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return new Message("用户 @" + username + " 注册成功", true);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException: " + e);
            return new Message("用户 @" + username + " 已存在", false);
        }
    }
}