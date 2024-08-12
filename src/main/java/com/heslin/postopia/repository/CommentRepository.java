package com.heslin.postopia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.Comment;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    Long findUserIdById(Long id);
}
