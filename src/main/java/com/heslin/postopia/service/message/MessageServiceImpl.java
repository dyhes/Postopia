package com.heslin.postopia.service.message;

import com.heslin.postopia.dto.UserMessage;
import com.heslin.postopia.jpa.model.Message;
import com.heslin.postopia.jpa.model.User;
import com.heslin.postopia.jpa.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Page<UserMessage> getMessages(User user, Pageable pageable) {
        return messageRepository.getMessages(user.getUsername(), pageable);
    }

    @Override
    @Transactional
    public void readMessages(User user, List<Long> ids) {
        messageRepository.readMessages(user.getUsername(), ids);
    }

    @Override
    @Transactional
    public void deleteMessages(User user, List<Long> ids) {
        messageRepository.deleteMessages(user.getUsername(),ids);
    }

    @Override
    @Transactional
    public void readAll(User user) {
        messageRepository.readAll(user.getUsername());
    }

    @Override
    public void deleteAllRead(User user) {
        messageRepository.deleteAllRead(user.getUsername());
    }

    @Override
    @Transactional
    public void saveAll(List<Message> messages) {
        messageRepository.saveAll(messages);
    }

    public static <T> List<List<T>> splitByStream(List<T> list, int chunkSize) {
        return IntStream.range(0, (list.size() + chunkSize - 1) / chunkSize)
        .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, list.size())))
        .collect(Collectors.toList());
    }

    @Override
    public void batchSave(List<Message> messages) {
        List<List<Message>> partition = splitByStream(messages, 1000);
        partition.forEach(messageRepository::saveAll);
    }
}
