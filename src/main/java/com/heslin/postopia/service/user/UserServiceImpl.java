package com.heslin.postopia.service.user;

import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Override
    public void updateUserNickName(Long id, String nickname) {
        userRepository.updateNickname(id, nickname);
    }


}
