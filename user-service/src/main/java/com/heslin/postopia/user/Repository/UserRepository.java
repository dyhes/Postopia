package com.heslin.postopia.user.Repository;

import com.heslin.postopia.common.dto.Avatar;
import com.heslin.postopia.common.dto.SearchUserInfo;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("select new com.heslin.postopia.user.dto.UserInfo(new com.heslin.postopia.common.dto.UserId(u.id), u.username, u.nickname, u.avatar, u.postCount, u.commentCount, u.credit, u.introduction, u.email, u.showEmail) from User u WHERE u.username = :username")
    UserInfo findUserInfoByUsername(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.nickname = :nickname WHERE u.id = :id")
    void updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.introduction = :intro WHERE u.id = :id")
    void updateIntroduction(@Param("id") Long id, @Param("intro")String introduction);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :id")
    void updateEmail(@Param("id") Long id, @Param("email") String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.avatar = :avatar WHERE u.id = :id")
    void updateAvatar(@Param("id") Long id, @Param("avatar") String url);

    @Query("select new com.heslin.postopia.common.dto.Avatar(u.username, u.avatar) from User u where u.username in ?1")
    List<Avatar> findAvatarsByUsernameIn(List<String> names);

    //@Query("select new com.heslin.postopia.user.dto.SearchUserInfo(u.username, u.avatar, u.introduction, u.postCount, u.commentCount, u.credit) from User u where u.username in ?1")
    List<SearchUserInfo> findSearchUserInfosByUsernameIn(List<String> names);
}
