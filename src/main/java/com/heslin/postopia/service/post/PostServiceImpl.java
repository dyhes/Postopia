package com.heslin.postopia.service.post;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.PostRepository;
import com.heslin.postopia.util.Pair;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;

    @Override
    public Pair<Message, Long> createPost(Space space, User user, String subject, String content) {
        var post = new Post();
        post.setSpace(space);
        post.setPoster(user);
        post.setSubject(subject);
        post.setContent(content);
        post = postRepository.save(post);
        return new Pair<>(new Message("Post created successfully", true), post.getId());
    }

    @Override
    public Message deletePost(User user, Long id) {
        var post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return new Message("对应的帖子不存在", false);
        }
        if (!Objects.equals(post.getPoster().getId(), user.getId())) {
            return new Message("You are not the author of this post", false);
        }
        deletePost(id);
        return new Message("删除成功", true);
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
    
}
