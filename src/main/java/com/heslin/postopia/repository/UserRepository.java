package com.heslin.postopia.repository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    public User findByUsername(String username);
}
