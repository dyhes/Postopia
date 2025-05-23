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

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<CommentOpinionHint> findOpinionHintsByIdIn(List<Long> list);

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
    List<Object[]> findSubsRaw(@Param("topIds") List<Long> topIds);

    default List<CommentPart> findSubs(List<Long> parentIds) {
        List<Object[]> results = findSubsRaw(parentIds);
        return results.stream()
        .map(row -> new CommentPart(
        ((Number) row[0]).longValue(),
        row[1] != null ? ((Number) row[1]).longValue() : null,
        ((Number) row[2]).longValue(),
        (String) row[3],
        (Boolean) row[4],
        ((Number) row[5]).longValue(),
        ((Number) row[6]).longValue(),
        (Instant) row[7]
        ))
        .collect(Collectors.toList());
    }

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
    List<Object[]> findByParentRecursiveRaw(@Param("commentId") Long commentId);

    default List<DeleteCommentInfo> findByParentRecursive(Long commentId) {
        List<Object[]> results = findByParentRecursiveRaw(commentId);
        return results.stream()
        .map(row -> new DeleteCommentInfo(
        ((Number) row[0]).longValue(),
        ((Number) row[1]).longValue(),
        (String) row[2]
        ))
        .collect(Collectors.toList());
    }

    List<SummaryCommentInfo> findSummaryByPostId(Long postId);
}
