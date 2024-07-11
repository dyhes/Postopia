package com.heslin.postopia.service.login;

import com.heslin.postopia.dto.Credential;

public interface LoginService {
    public Credential login(String username, String password);
}
