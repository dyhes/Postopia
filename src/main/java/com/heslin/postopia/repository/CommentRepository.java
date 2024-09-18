package com.heslin.postopia.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.Comment;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    Long findUserIdById(Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.positiveCount = c.positiveCount + 1 where c.id = :id")
    void likeComment(@Param("id")Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.negativeCount = c.negativeCount + 1 where c.id = :id")
    void disLikeComment(@Param("id")Long id);
}
