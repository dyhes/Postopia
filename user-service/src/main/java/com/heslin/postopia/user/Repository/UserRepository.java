package com.heslin.postopia.user.Repository;

import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("select new com.heslin.postopia.user.dto.UserInfo(new com.heslin.postopia.common.dto.UserId(u.id), u.username, u.nickname, u.avatar, u.postCount, u.commentCount, u.credit, u.introduction, u.email, u.showEmail) from User u WHERE u.username = :username")
    UserInfo findUserInfoByUsername(@Param("username") String username);
}
