package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.post.SpacePostSummary;
import com.heslin.postopia.jpa.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p.user.id from Post p where p.id =:id")
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Post p set p.subject = :subject, p.content = :content where p.id = :id and p.user.id = :uid and p.isArchived = false ")
    int updateSubjectAndContent(@Param("id")Long id, @Param("uid")Long uid, @Param("subject")String subject, @Param("content")String content);


    @Query("""
             select
                 new com.heslin.postopia.dto.post.PostInfo(p.subject, p.content, p.positiveCount, p.negativeCount, p.commentCount, u.username, u.nickname, u.avatar,
                     CASE
                         WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                         WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                         ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                     END, p.isArchived)
            from Post p
            JOIN p.user u
            LEFT JOIN PostOpinion o on o.user.id = :uid and o.post.id = :id
            where p.id = :id
            
            """)
    Optional<PostInfo> findPostInfoById(@Param("id") Long id, @Param("uid") Long userId);

    @Query("""
            select
            new com.heslin.postopia.dto.post.SpacePostSummary(p.id, p.subject, SUBSTRING(p.content, 1, 100), p.positiveCount, p.negativeCount, p.commentCount, u.username, u.nickname, u.avatar,
                    CASE
                        WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                        WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                        ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                    END, p.createdAt, p.isArchived)
                    from Post p
                    JOIN p.user u
                    LEFT JOIN PostOpinion o on o.post.id = p.id and o.user.id = :uid
                    where p.space.id = :id
            """)
    Page<SpacePostSummary> findPostSummariesBySpaceId(@Param("id") Long id, @Param("uid") Long userId, Pageable pageable);


    @Query("""
            select new com.heslin.postopia.dto.post.PostSummary(s.name, p.id, p.subject, SUBSTRING(p.content, 1, 100), p.positiveCount, p.negativeCount, p.commentCount, p.createdAt, p.isArchived)
                from Post p
                join p.user u
                join p.space s
                where u.id = :uid
            """)
    Page<PostSummary> findPostSummariesBySelf(@Param("uid") Long id, Pageable pageable);



    @Query("""
            select new com.heslin.postopia.dto.post.UserPostSummary(s.name, p.id, p.subject, SUBSTRING(p.content, 1, 100), p.positiveCount, p.negativeCount, p.commentCount,
                    CASE
                        WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                        WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                        ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                    END, p.createdAt, p.isArchived)
                from Post p
                join p.user u
                join p.space s
                left join PostOpinion o on o.post.id = p.id and o.user.id = :sid
                where u.id = :qid
            """)
    Page<PostSummary> findPostSummariesByUserId(@Param("qid") Long qid, @Param("sid") Long sid, Pageable pageable);

    @Query("select new com.heslin.postopia.elasticsearch.dto.SearchedPostInfo(p.id, p.subject, p.positiveCount, p.negativeCount, p.commentCount, p.space.avatar, p.createdAt) from Post p where p.id in :ids")
    List<SearchedPostInfo> findPostInfosInSearch(List<Long> ids);

    @Transactional
    @Modifying
    @Query("delete from Post p where p.id = :id")
    int deletePost(@Param("id") Long id);

    @Query("select new com.heslin.postopia.dto.AuthorHint(p.id, p.user.username, p.subject) from Post p where p.id in :ids")
    List<AuthorHint> getAuthorHints(@Param("ids") List<Long> postIds);

    @Query("select count(*) from Post p where p.id = :pid and p.isArchived = :isArchived")
    int checkPostArchiveStatus(@Param("pid") Long postId, @Param("isArchived") boolean isArchived);

    @Modifying
    @Transactional
    @Query("update Post p set p.isArchived = :isArchived where p.id = :pid")
    void updateArchiveStatus(@Param("pid") Long postId, @Param("isArchived") boolean isArchived);
}
