package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.jpa.model.Comment;
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
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
            select c.user.id from Comment c where c.id=:id
            """)
    Optional<Long> findUserIdById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Comment c set c.negativeCount = c.negativeCount + 1 where c.id = :id and c.post.id = :pid and c.user.id = :uid")
    int deleteComment(@Param("id") Long id, @Param("pid") Long pid, @Param("uid") Long uid);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentInfo(c.id,c.content, c.createdAt, u.id, u.nickname, u.avatar,
                        CASE
                            WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                            WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                            ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                            END, c.positiveCount, c.negativeCount) from Comment c
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
                       c.parent_id AS parent_id,  -- 用于递归连接
                       c.positive_count,
                       c.negative_count,
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
                       child.parent_id AS parent_id,
                       child.positive_count,
                       child.negative_count,
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
                   parent_id,
                   positive_count,
                   negative_count,
               FROM comment_tree
               ORDER BY created_at ASC;
                                """, nativeQuery = true)
    List<Object[]> findChildrenByCommentIds(@Param("cids") List<Long> commentIds, @Param("uid") Long userId);

    @Query("""
    select new com.heslin.postopia.dto.comment.UserCommentSummary(
        c.id, s.id, s.name, p.id, p.subject,SUBSTRING(c.content, 1, 100),
                pu.id,
                pu.nickname,
                c.createdAt,
                c.positiveCount,
                c.negativeCount,
                CASE
                   WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                   WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                   ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
               END) from Comment c
                JOIN c.post p
                JOIN p.space s
                LEFT JOIN c.parent.user pu
                LEFT JOIN CommentOpinion o on o.user.id = :sid and o.comment.id = c.id
                where c.user.id = :qid
    """)
    Page<CommentSummary> findCommentsByUser(@Param("qid") Long queryId,@Param("sid") Long selfId, Pageable pageable);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentSummary(
                c.id, s.id, s.name, p.id, p.subject, SUBSTRING(c.content, 1, 100),
                pu.id,
                pu.nickname,
                c.createdAt, c.positiveCount, c.negativeCount)
                from Comment c
                JOIN c.post p
                JOIN p.space s
                LEFT JOIN c.parent.user pu
                where c.user.id = :uid
            """)
    Page<CommentSummary> findCommentsBySelf(@Param("uid") Long id, Pageable pageable);

}
