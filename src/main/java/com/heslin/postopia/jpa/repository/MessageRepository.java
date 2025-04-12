package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.UserMessage;
import com.heslin.postopia.jpa.model.Message;
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
    SELECT new com.heslin.postopia.dto.UserMessage(m.id, m.content, m.isRead, m.createdAt)
           FROM Message m WHERE m.username = :username order by m.isRead asc, m.createdAt desc
    """)
    Page<UserMessage> getMessages(@Param("username") String username, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        update Message m set m.isRead = true
        where m.username = :username and m.id in (:ids)
    """)
    void readMessages(@Param("username") String username, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("""
        delete Message m
        where m.username = :username and m.id in (:ids)
    """)
    void deleteMessages(@Param("username") String username, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("""
        delete Message m
        where m.username = :username and m.isRead = true
    """)
    void deleteAllRead(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("""
        update Message m set m.isRead = true
        where m.username = :username
    """)
    void readAll(@Param("username") String username);
}
