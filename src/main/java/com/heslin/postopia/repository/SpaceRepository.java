package com.heslin.postopia.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.heslin.postopia.model.Space;

@Repository
public interface SpaceRepository extends CrudRepository<Space, Long> {
    Space findByName(String name);
}
