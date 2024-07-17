package com.heslin.postopia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.Post;

@Repository
public interface PostRepository extends CrudRepository<Post, Long>{

}
