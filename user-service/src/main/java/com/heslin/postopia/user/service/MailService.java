package com.heslin.postopia.user.service;

import com.heslin.postopia.common.redis.RedisService;
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RefreshScope
public class MailService {
    private final JavaMailSender emailSender;
    private final RedisService redisService;

    @Value("${postopia.mail.from}")
    private String from;

    @Value("${postopia.mail.subject}")
    private String subject;

    @Autowired
    public MailService(JavaMailSender emailSender, RedisService redisService) {
        this.emailSender = emailSender;
        this.redisService = redisService;
    }

    Integer generateAuthCode(String address) {
        SecureRandom random = new SecureRandom(address.getBytes());
        return 100000 + random.nextInt(900000);
    }

    public void sendAuthCode(Long userId, String username, String address) {
        SimpleMailMessage message = new SimpleMailMessage();
        String authCode = generateAuthCode(address).toString();
        redisService.setByMinute(authCode, userId + ";" + address, 10);
        String text = "用户 @%s 正在尝试绑定此邮箱账号，验证码为 %s \n 请在10分钟内验证。".formatted(username, authCode);
        message.setFrom(from);
        message.setTo(address);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
