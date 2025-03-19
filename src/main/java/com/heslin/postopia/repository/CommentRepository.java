package com.heslin.postopia.repository;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    @Query("""
            select c.user.id from Comment c where c.id=:id
            """)
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.positiveCount = c.positiveCount + 1 where c.id = :id")
    void likeComment(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.negativeCount = c.negativeCount + 1 where c.id = :id")
    void disLikeComment(@Param("id") Long id);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentSummary(c.id,c.content, c.createdAt, p.space.id, p.id, p.subject, u.id, u.nickname, u.avatar) from Comment c JOIN c.post p JOIN c.user u where u.id = :uid
            """)
    Page<CommentSummary> findCommentsByUserId(@Param("uid") Long id, Pageable pageable);

    @EntityGraph(attributePaths = {"children"})
    @Query("""
                select new com.heslin.postopia.dto.comment.CommentInfo(c.id,c.content, c.createdAt, u.id, u.nickname, u.avatar) from Comment c JOIN c.user u where c.post.id = :pid and c.parent IS NULL
            """)
    Page<CommentInfo> findByPostId(@Param("pid") Long postId, Pageable pageable);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentInfo(c.id,c.content, c.createdAt, u.id, u.nickname, u.avatar) from Comment c JOIN c.user u where c.parent.id = :cid
            """)
    List<CommentInfo> findChildrenByCommentId(@Param("cid") Long commentId);
}
