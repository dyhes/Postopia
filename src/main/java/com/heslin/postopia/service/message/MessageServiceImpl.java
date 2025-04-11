package com.heslin.postopia.service.message;

import com.heslin.postopia.dto.UserMessage;
import com.heslin.postopia.jpa.model.Message;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Page<UserMessage> getMessages(User user, Pageable pageable) {
        return messageRepository.getMessages(user.getId(), pageable);
    }

    @Override
    @Transactional
    public void readMessages(User user, List<Long> ids) {
        messageRepository.readMessages(user.getId(), ids);
    }

    @Override
    @Transactional
    public void deleteMessages(User user, List<Long> ids) {
        messageRepository.deleteMessages(user.getId(),ids);
    }

    @Override
    @Transactional
    public void readAll(User user) {
        messageRepository.readAll(user.getId());
    }

    @Override
    public void deleteAllRead(User user) {
        messageRepository.deleteAllRead(user.getId());
    }

    @Override
    @Transactional
    public void saveAll(List<Message> messages) {
        messageRepository.saveAll(messages);
    }
}
