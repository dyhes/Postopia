package com.heslin.postopia.post.repository;

import com.heslin.postopia.post.dto.PostOpinionHint;
import com.heslin.postopia.post.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Transactional
    @Query("delete from Post p where p.id = ?1")
    boolean deletePost(Long postId);

    List<PostOpinionHint> findPOHByIdIn(Collection<Long> ids);
}
