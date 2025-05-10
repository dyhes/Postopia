package com.heslin.postopia.service.signup;

import com.heslin.postopia.dto.ResMessage;
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
    public ResMessage signup(String username, String password) {
        try {
            User user = User.builder().username(username).nickname(username).avatar(defaultUserAvatar)
            .postCount(0L)
            .commentCount(0L)
            .credit(0L)
            .password(passwordEncoder.encode(password))
            .showEmail(false)
            .build();
            userRepository.save(user);
            kafkaService.sendToDocCreate("user", user.getUsername(), new UserDoc(user.getUsername(), user.getUsername(), user.getNickname()));
            return new ResMessage("用户 @" + username + " 注册成功", true);
        } catch (DataIntegrityViolationException e) {
            System.out.println("DataIntegrityViolationException: " + e);
            return new ResMessage("用户 @" + username + " 已存在", false);
        }
    }
}