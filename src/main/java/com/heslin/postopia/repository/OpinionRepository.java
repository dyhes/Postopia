package com.heslin.postopia.repository;

import com.heslin.postopia.model.opinion.Opinion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpinionRepository extends CrudRepository<Opinion, Long> {
}
