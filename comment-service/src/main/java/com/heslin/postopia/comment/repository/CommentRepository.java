package com.heslin.postopia.comment.repository;

import com.heslin.postopia.comment.model.Comment;
import com.heslin.postopia.comment.dto.CommentOpinionHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select new com.heslin.postopia.comment.dto.CommentOpinionHint(c.id, c.postId, c.userId, SUBSTRING(c.content, 100)) from Comment c where c.id in ?1")
    List<CommentOpinionHint> findOpinionHints(List<Long> list);
}
