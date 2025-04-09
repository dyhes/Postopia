package com.heslin.postopia.service.signup;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.elasticsearch.model.UserDoc;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.UserRepository;
import com.heslin.postopia.kafka.KafkaService;
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
    private final KafkaService kafkaService;

    @Autowired
    public SignupServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, @Value("${postopia.avatar.user}") String defaultUserAvatar, KafkaService kafkaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultUserAvatar = defaultUserAvatar;
        this.kafkaService = kafkaService;
    }

    @Override
    public Message signup(String username, String password) {
        User user = new User();
        try {
            user.setUsername(username);
            user.setNickname(username);
            user.setAvatar(defaultUserAvatar);
            user.setShowEmail(false);
            user.setPassword(passwordEncoder.encode(password));
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException: " + e);
            return new Message("用户 @" + username + " 已存在", false);
        }
        kafkaService.sendToCreate("user", user.getUsername(), new UserDoc(user.getUsername(), user.getUsername(), user.getNickname()));
        return new Message("用户 @" + username + " 注册成功", true);
    }
}