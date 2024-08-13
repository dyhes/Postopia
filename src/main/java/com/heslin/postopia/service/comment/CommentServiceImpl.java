package com.heslin.postopia.service.comment;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.CommentRepository;
import com.heslin.postopia.repository.PostRepository;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    @Override
    public Comment replyToPost(Post post, String content,@AuthenticationPrincipal User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        postRepository.addComment(post.getId());
        comment.setContent(content);
        comment = commentRepository.save(comment);
        return comment;
    }

    @Override
    public void reply(Post post, Comment parent, String content,@AuthenticationPrincipal User user) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        postRepository.addComment(post.getId());
        comment.setContent(content);
        comment.setParent(parent);
        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void checkAuthority(Long id, User user) {
        if (!Objects.equals(commentRepository.findUserIdById(id), user.getId())) {
            throw new ForbiddenException("You are not the owner of this comment");
        }
    }
 
    
}
