package com.heslin.postopia.message.service;

import com.heslin.postopia.message.dto.MessageInfo;
import com.heslin.postopia.message.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService{
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    
    public Page<MessageInfo> getMessages(Long userId, Pageable pageable) {
        return messageRepository.findByUserId(userId, pageable);
    }

    
    @Transactional
    public void readMessages(Long userId, List<Long> ids) {
        messageRepository.readMessages(userId, ids);
    }

    
    @Transactional
    public void deleteMessages(Long userId, List<Long> ids) {
        messageRepository.deleteMessages(userId,ids);
    }

    
    @Transactional
    public void readAll(Long userId) {
        messageRepository.readAll(userId);
    }

    public void deleteAllRead(Long userId) {
        messageRepository.deleteAllRead(userId);
    }
    
//    @Transactional
//    public void saveAll(List<Message> messages) {
//        messageRepository.saveAll(messages);
//    }

//    public static <T> List<List<T>> splitByStream(List<T> list, int chunkSize) {
//        return IntStream.range(0, (list.size() + chunkSize - 1) / chunkSize)
//        .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, list.size())))
//        .collect(Collectors.toList());
//    }
//
//
//    public void batchSave(List<Message> messages) {
//        List<List<Message>> partition = splitByStream(messages, 1000);
//        partition.forEach(messageRepository::saveAll);
//    }
}
