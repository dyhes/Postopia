package com.heslin.postopia.comment.service;

import com.heslin.postopia.comment.repository.CommentRepository;
import com.heslin.postopia.comment.dto.CommentOpinionHint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentOpinionHint> getOpinionHints(List<Long> list) {
        return commentRepository.findOpinionHints(list);
    }

    //
//    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
//        return opinionRepository.getCommentOpinionsByUser(id, statuses, pageable);
//    }
}
