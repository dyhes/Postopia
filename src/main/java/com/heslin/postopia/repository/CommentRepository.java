package com.heslin.postopia.repository;

import com.heslin.postopia.dto.UserCommentInfo;
import com.heslin.postopia.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
                select new com.heslin.postopia.dto.UserCommentInfo(c.id,c.content, c.createdAt, p.space.id, p.id, p.subject, u.id, u.nickname, u.avatar) from Comment c JOIN c.post p JOIN c.user u where u.id = :uid
            """)
    Page<UserCommentInfo> findCommentsByUserId(@Param("uid") Long id, Pageable pageable);
}
