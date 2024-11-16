package com.heslin.postopia.repository;

import com.heslin.postopia.dto.CommentInfo;
import com.heslin.postopia.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    Long findUserIdById(Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.positiveCount = c.positiveCount + 1 where c.id = :id")
    void likeComment(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.negativeCount = c.negativeCount + 1 where c.id = :id")
    void disLikeComment(@Param("id") Long id);

    @Query("""
                select new com.heslin.postopia.dto.CommentInfo(c.id,c.content, c.createdAt, p.space.id, p.id, p.subject, u.id, u.nickname, u.avatar) from Comment c JOIN c.post p JOIN c.user u where u.id = :uid
            """)
    Page<CommentInfo> findCommentsByUserId(@Param("uid") Long id, Pageable pageable);


    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.children WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findAllByPostId(@Param("postId") Long postId);
}
