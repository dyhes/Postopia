package com.heslin.postopia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.model.Post;

import jakarta.transaction.Transactional;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>{
    Optional<Long> findUserIdById(Long id);

    @Modifying
    @Transactional
    @Query("update Post p set p.status = :status where p.id = :id")
    void updateStatus(@Param("id")Long id, @Param("status")PostStatus status);

    @Modifying
    @Transactional
    @Query("update Post p set p.subject = :subject, p.content = :content where p.id = :id")
    void updateSubjectAndContent(@Param("id")Long id, @Param("subject")String subject, @Param("content")String content);

    Optional<PostStatus> findStatusById(Long id);
}
