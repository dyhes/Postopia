package com.heslin.postopia.message.repository;

import com.heslin.postopia.message.dto.MessageInfo;
import com.heslin.postopia.message.model.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
    SELECT new com.heslin.postopia.message.dto.MessageInfo(m.id, m.content, m.read, m.createdAt)
           FROM Message m WHERE m.userId = :userId order by m.read asc, m.createdAt desc
    """)
    Page<MessageInfo> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        update Message m set m.read = true
        where m.userId = :userId and m.id in (:ids)
    """)
    void readMessages(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("""
        delete Message m
        where m.userId = :userId and m.id in (:ids)
    """)
    void deleteMessages(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("""
        delete Message m
        where m.userId = :userId and m.read = true
    """)
    void deleteAllRead(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("""
        update Message m set m.read = true
        where m.userId = :userId
    """)
    void readAll(@Param("userId") Long userId);

    Long countByUserIdAndReadIsFalse(Long userId);
}
