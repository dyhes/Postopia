package com.heslin.postopia.space.repository;

import com.heslin.postopia.space.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
}
