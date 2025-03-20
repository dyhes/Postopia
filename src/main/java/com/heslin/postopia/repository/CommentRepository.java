package com.heslin.postopia.repository;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.model.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    @Query("""
            select c.user.id from Comment c where c.id=:id
            """)
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.positiveCount = c.positiveCount + 1 where c.id = :id")
    void likeComment(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.negativeCount = c.negativeCount + 1 where c.id = :id")
    void disLikeComment(@Param("id") Long id);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentSummary(c.id,c.content, c.createdAt, p.space.id, p.id, p.subject, u.id, u.nickname, u.avatar) from Comment c JOIN c.post p JOIN c.user u where u.id = :uid 
            """)
    Page<CommentSummary> findCommentsByUserId(@Param("uid") Long id, Pageable pageable);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentInfo(c.id,c.content, c.createdAt, u.id, u.nickname, u.avatar,
                        CASE
                            WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                            WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                            ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                            END) from Comment c
                                   JOIN c.user u
                                   LEFT JOIN CommentOpinion o on o.user.id = :uid and o.comment.id = c.id
                                   where c.post.id = :pid and c.parent IS NULL
            """)
    Page<CommentInfo> findByPostId(@Param("pid") Long postId, @Param("uid") Long userId, Pageable pageable);


    @Query(value = """

            WITH RECURSIVE comment_tree AS (
                   -- 初始查询：选择指定父评论的直接子评论
                   SELECT
                       c.id,
                       c.content,
                       c.created_at,
                       u.id AS user_id,
                       u.nickname AS nickname,
                       u.avatar AS avatar,
                       CASE
                           WHEN o.id IS NULL THEN 'NIL'
                           WHEN o.is_positive = TRUE THEN 'POSITIVE'
                           ELSE 'NEGATIVE'
                       END AS opinion_status,
                       c.parent_id AS parent_id  -- 用于递归连接
                   FROM
                       comments c
                   JOIN
                       users u ON c.user_id = u.id
                   LEFT JOIN
                       comment_opinions o ON o.comment_id = c.id AND o.user_id = :uid
                   WHERE
                       c.parent_id IN (:cids)
        
                   UNION ALL
                  
                   -- 递归查询：逐层获取嵌套子评论
                   SELECT
                       child.id,
                       child.content,
                       child.created_at,
                       u_child.id AS user_id,
                       u_child.nickname AS nickname,
                       u_child.avatar AS avatar,
                       CASE
                           WHEN o_child.id IS NULL THEN 'NIL'
                           WHEN o_child.is_positive = TRUE THEN 'POSITIVE'
                           ELSE 'NEGATIVE'
                       END AS opinion_status,
                       child.parent_id AS parent_id
                   FROM
                       comments child
                   JOIN
                       comment_tree parent ON parent.id = child.parent_id  -- 关键递归连接
                   JOIN
                       users u_child ON child.user_id = u_child.id
                   LEFT JOIN
                       comment_opinions o_child ON o_child.comment_id = child.id AND o_child.user_id = :uid
               )
               SELECT
                   id,
                   content,
                   created_at,
                   user_id,
                   nickname,
                   avatar,
                   opinion_status,
                   parent_id
               FROM comment_tree
               ORDER BY created_at ASC;
                                """, nativeQuery = true)
    List<Object[]> findChildrenByCommentIds(@Param("cids") List<Long> commentIds, @Param("uid") Long userId);
}
