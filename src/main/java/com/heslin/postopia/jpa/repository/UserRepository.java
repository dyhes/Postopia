package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.Avatar;
import com.heslin.postopia.dto.user.UserInfo;
import com.heslin.postopia.jpa.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
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

    @Query("select new com.heslin.postopia.dto.Avatar(u.avatar, u.username) from User u where u.username in ?1")
    List<Avatar> findUserAvatars(List<String> names);
}
