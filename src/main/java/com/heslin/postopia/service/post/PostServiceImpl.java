package com.heslin.postopia.service.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.exception.ResourceNotFoundException;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.PostRepository;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.util.Pair;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentService commentService;

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
        var uid = postRepository.findUserIdById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!uid.equals(user.getId())) {
            throw new ForbiddenException();
        }
        
    }

    @Override
    public void archivePost(Long id) {
        postRepository.updateStatus(id, PostStatus.ARCHIVED);
    }

    @Override
    public void unarchivePost(Long id) {
        postRepository.updateStatus(id, PostStatus.PUBLISHED);
    }

    @Override
    public void updatePost(Long id, String subject, String content) {
        postRepository.updateSubjectAndContent(id, subject, content);
    }

    @Override
    public void checkPostStatus(Long id) {
        var status = postRepository.findStatusById(id).orElseThrow(() -> new ForbiddenException("Post not found"));
        if (status != PostStatus.PUBLISHED) {
            throw new ForbiddenException();
        }

    }

    @Override
    public void replyPost(Long id, String content) {
        var post = new Post();
        post.setId(id);
        commentService.replyToPost(post, content, null);
    }

    @Override
    public PostInfo getPostInfo(Long id) {
        return postRepository.findPostInfoById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    @Override
    public Page<PostSummary> getPosts(Long id, Pageable pageable) {
        return postRepository.findPostSummariesBySpaceId(id, pageable);
    }
    
}
