package com.heslin.postopia.repository;

import com.heslin.postopia.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    public User findByUsername(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.nickname = :nickname WHERE u.id = :id")
    void updateNickname(@Param("id") Long id, @Param("nickname") String nickname);
}
