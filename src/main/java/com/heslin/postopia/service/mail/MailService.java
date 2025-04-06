package com.heslin.postopia.service.mail;

import com.heslin.postopia.jpa.model.User;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendAuthenticationCode(String address, User user) throws MessagingException;
}
