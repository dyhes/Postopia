package com.heslin.postopia.user.Repository;

import com.heslin.postopia.user.dto.UserAvatar;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.dto.UserDetail;
import com.heslin.postopia.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    //@Query("select new com.heslin.postopia.user.dto.UserAvatar(new com.heslin.postopia.common.dto.UserId(u.id), u.avatar) from User u where u.id in ?1")
    List<UserAvatar> findAvatarsByUserIdIn(List<Long> ids);

    //@Query("select u.id, u.username, u.nickname, u.avatar, u.postCount, u.commentCount, u.credit, u.introduction, u.email, u.showEmail, u.createdAt,  from User u WHERE u.id = ?1")
    UserDetail findUserDetailByUserId(Long userId);

    List<UserInfo> findUserInfosByUserIdIn(Collection<Long> userIds);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.nickname = :nickname WHERE u.userId = :id")
    void updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.introduction = :intro WHERE u.userId = :id")
    void updateIntroduction(@Param("id") Long id, @Param("intro")String introduction);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.userId = :id")
    void updateEmail(@Param("id") Long id, @Param("email") String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.avatar = :avatar WHERE u.userId = :id")
    void updateAvatar(@Param("id") Long id, @Param("avatar") String url);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.showEmail = :show WHERE u.userId = :id")
    void updateShowStatus(@Param("show") boolean show, @Param("id") Long id);
}
