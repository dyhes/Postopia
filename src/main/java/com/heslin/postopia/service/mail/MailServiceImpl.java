package com.heslin.postopia.service.mail;

import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.service.redis.RedisService;
import com.heslin.postopia.util.Pair;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private RedisService redisService;

    @Value("${postopia.mail.from}")
    private String from;

    @Value("${postopia.mail.auth.subject}")
    private String subject;

    int generateAuthCode(String address) {
        SecureRandom random = new SecureRandom(address.getBytes());
        return 100000 + random.nextInt(900000);
    }

    @Override
    public void sendAuthenticationCode(String address, User user) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        int authCode = generateAuthCode(address);
        redisService.setByMinute(address, new Pair<Long, Integer>(user.getId(), authCode).toString(), 10);
        String text = "User @" + user.getUsername() + " is trying to bind with the Email Account, Verification Code " + authCode + "\n Please Verify in 10 minutes.";
        message.setFrom(from);
        message.setTo(address);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
