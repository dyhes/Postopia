package com.heslin.postopia.comment.repository;

import com.heslin.postopia.comment.dto.*;
import com.heslin.postopia.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select new com.heslin.postopia.comment.dto.CommentOpinionHint(c.id, c.postId, c.userId, SUBSTRING(c.content, 100)) from Comment c where c.id in ?1")
    List<CommentOpinionHint> findOpinionHints(List<Long> list);

    @Query("select count(*) from Comment c where c.id = :cid and c.isPined = :isPined")
    int checkCommentPinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);

    @Modifying
    @Transactional
    @Query("update Comment c set c.isPined = :isPined where c.id = :cid")
    void updateCommentPinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);

    Page<SpaceCommentPart> findByUserId(Long userId, Pageable pageable);

    List<SpaceCommentPart> findByIdIn(Collection<Long> ids);

    List<SearchCommentPart> findSearchByIdIn(Collection<Long> ids);

    Page<CommentPart> findByPostIdAndParentIdIsNull(Long postId, Pageable pageable);

    @Query(value =
        """
        WITH RECURSIVE comment_tree AS (
            SELECT
                c.id,
                c.parent_id as parentId,
                c.user_id as userId,
                c.content,
                c.is_pined as isPined,
                c.positive_count as positiveCount,
                c.negative_count as negativeCount,
                c.created_at as createdAt
            FROM
                comments c
            WHERE
                c.parent_id IN (:topIds)

            UNION ALL

            SELECT
                child.id,
                child.parent_id as parentId,
                child.user_id as userId,
                child.content,
                child.is_pined as isPined,
                child.positive_count as positiveCount,
                child.negative_count as negativeCount,
                child.created_at as createdAt
            FROM
                comments child
            JOIN
                comment_tree parent ON parent.id = child.parent_id
        )
        SELECT
            id,
            parentId,
            userId,
            content,
            isPined,
            positiveCount,
            negativeCount,
            createdAt
        FROM comment_tree
        """, nativeQuery = true)
    List<CommentPart> findSubs(@Param("topIds") List<Long> topIds);

    List<DeleteCommentDetail> findByPostIdIn(Collection<Long> postIds);

    @Transactional
    @Modifying
    @Query("delete Comment c where c.id in ?1")
    int deleteByIdIn(List<Long> list);

    @Query(value = """
    WITH RECURSIVE comment_tree AS (
        SELECT
            c.id,
            c.user_id as userId,
            c.content
        FROM
            comments c
        WHERE
            c.id = :commentId
            
        UNION ALL
        
        SELECT
            child.id,
            child.user_id as userId,
            child.content
        FROM
            comments child
        JOIN
            comment_tree parent ON parent.id = child.parent_id
    )
    SELECT
        id,
        userId,
        content
    FROM comment_tree
    """, nativeQuery = true)
    List<DeleteCommentInfo> findByParentRecursive(@Param("commentId") Long commentId);

    List<SummaryCommentInfo> findSummaryByPostId(Long postId);
}
