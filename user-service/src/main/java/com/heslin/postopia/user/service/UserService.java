package com.heslin.postopia.user.service;

import com.heslin.postopia.common.dto.response.ResMessage;
import com.heslin.postopia.user.Repository.UserRepository;
import com.heslin.postopia.user.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public ResMessage signup(UserController.SignupRequest signupRequest) {
        return null;
    }
}
