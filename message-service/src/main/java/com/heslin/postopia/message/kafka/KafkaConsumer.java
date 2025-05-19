package com.heslin.postopia.message.kafka;

import com.heslin.postopia.message.model.Message;
import com.heslin.postopia.message.service.MessageService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class KafkaConsumer {
    private final MessageService messageService;

    @Autowired
    public KafkaConsumer(MessageService messageService) {
        this.messageService = messageService;
    }

    @KafkaListener(topics = "msg", containerFactory = "batchLSFactory")
    @Transactional
    protected void processMessages(List<ConsumerRecord<Long, String>> records) {
        Instant current = Instant.now();
        List<Message> messages = records.stream().map(record -> Message.builder().userId(record.key()).content(record.value()).isRead(false).createdAt(current).build()
        ).toList();
        messageService.saveAll(messages);
    }
}
