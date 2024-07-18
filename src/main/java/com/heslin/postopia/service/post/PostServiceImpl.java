package com.heslin.postopia.service.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.exception.ForbiddenException;
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
    public Pair<Long, Message> createPost(boolean isDraft, Space space, User user, String subject, String content) {
        var post = new Post();
        post.setSpace(space);
        post.setUser(user);
        post.setSubject(subject);
        post.setContent(content);
        post.setStatus(isDraft ? PostStatus.DRAFT : PostStatus.PUBLISHED);
        post = postRepository.save(post);
        return new Pair<>(post.getId(), new Message("Post created successfully", true));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public void authorize(User user, Long postId) {
        if (!postRepository.findUserIdById(postId).orElse(null).equals(user.getId())) {
            throw new ForbiddenException("Access Denied");
        }
        
    }

    @Override
    public void archivePost(Long id) {
        postRepository.updatePostStatus(id, PostStatus.ARCHIVED);
    }

    @Override
    public void unarchivePost(Long id) {
        postRepository.updatePostStatus(id, PostStatus.PUBLISHED);
    }
    
}
