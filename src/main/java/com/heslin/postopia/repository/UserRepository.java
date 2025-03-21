package com.heslin.postopia.repository;

import com.heslin.postopia.dto.user.UserInfo;
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

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :id")
    void updateEmail(@Param("id") Long id, @Param("email") String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.avatar = :avatar WHERE u.id = :id")
    void updateAvatar(@Param("id") Long id, @Param("avatar") String url);

    UserInfo findUserInfoById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.showEmail = :show WHERE u.id = :id")
    void updateShowStatusById(@Param("show") boolean show, @Param("id") Long id);
}
