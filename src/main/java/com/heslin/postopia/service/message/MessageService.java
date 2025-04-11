package com.heslin.postopia.service.message;


import com.heslin.postopia.dto.UserMessage;
import com.heslin.postopia.jpa.model.Message;
import com.heslin.postopia.jpa.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {
    Page<UserMessage> getMessages(User user, Pageable pageable);

    void readMessages(User user, List<Long> ids);

    void deleteMessages(User user, List<Long> ids);

    void readAll(User user);

    void deleteAllRead(User user);

    void saveAll(List<Message> messages);
}
