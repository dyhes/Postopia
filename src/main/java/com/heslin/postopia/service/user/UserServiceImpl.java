package com.heslin.postopia.service.user;

import org.springframework.beans.factory.annotation.Autowired;

import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.UserRepository;

public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).get();
    }

}
