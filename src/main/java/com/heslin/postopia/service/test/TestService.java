package com.heslin.postopia.service.test;

import com.heslin.postopia.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @PreAuthorize("isAuthenticated()")
    public String testDeep(@AuthenticationPrincipal User user) {
        return user + " successfully accessed in service layer";
    }
}
