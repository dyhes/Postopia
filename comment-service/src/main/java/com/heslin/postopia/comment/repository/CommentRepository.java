package com.heslin.postopia.comment.repository;

import com.heslin.postopia.comment.dto.CommentOpinionHint;
import com.heslin.postopia.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select new com.heslin.postopia.comment.dto.CommentOpinionHint(c.id, c.postId, c.userId, SUBSTRING(c.content, 100)) from Comment c where c.id in ?1")
    List<CommentOpinionHint> findOpinionHints(List<Long> list);

    @Query("select count(*) from Comment c where c.id = :cid and c.isPined = :isPined")
    int checkCommentPinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);

    @Modifying
    @Transactional
    @Query("update Comment c set c.isPined = :isPined where c.id = :cid")
    void updateCommentPinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);
}
