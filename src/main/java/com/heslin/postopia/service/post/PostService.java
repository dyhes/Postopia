package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.util.Pair;

public interface PostService {
    Pair<Message, Long> createPost(Space space, User user, String subject, String content);
    Message deletePost(User user, Long id);
    void deletePost(Long id);
}
