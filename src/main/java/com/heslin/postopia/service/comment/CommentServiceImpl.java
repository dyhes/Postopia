package com.heslin.postopia.service.comment;

import java.util.Objects;

import com.heslin.postopia.model.opinion.CommentOpinion;
import com.heslin.postopia.model.opinion.Opinion;
import com.heslin.postopia.service.opinion.OpinionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import com.heslin.postopia.repository.CommentRepository;
import com.heslin.postopia.repository.PostRepository;

import jakarta.transaction.Transactional;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private OpinionService opinionService;

    @Override
    @Transactional
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
    @Transactional
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

    @Override
    public void likeComment(Long id) {
        addCommentOpinion(id, true, null);
    }

    @Override
    public void disLikeComment(Long id) {
        addCommentOpinion(id, false, null);
    }

    private void addCommentOpinion(Long id, boolean opinion, @AuthenticationPrincipal User user) {
        if (opinion) {
            commentRepository.likeComment(id);
        } else {
            commentRepository.disLikeComment(id);
        }
        CommentOpinion postOpinion = new CommentOpinion();
        postOpinion.setUser(user);
        postOpinion.setComment(new Comment(id));
        postOpinion.setPositive(opinion);
        opinionService.saveOpinion(postOpinion);
    }


}
