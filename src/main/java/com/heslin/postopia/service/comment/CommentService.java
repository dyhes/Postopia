package com.heslin.postopia.service.comment;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;

public interface  CommentService {
    Comment replyToPost(Post post, String content, @AuthenticationPrincipal User user);
}
