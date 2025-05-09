package com.heslin.postopia.jpa.repository;

import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.dto.comment.CommentInfo;
import com.heslin.postopia.dto.comment.CommentSummary;
import com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo;
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
    @Query("delete Comment c where c.id = :id")
    int deleteComment(@Param("id") Long id);

    @Query("""
                select new com.heslin.postopia.dto.comment.CommentInfo(c.id, null, c.content, c.createdAt, u.username, u.nickname, u.avatar,
                        CASE
                            WHEN o.id IS NULL THEN com.heslin.postopia.enums.OpinionStatus.NIL
                            WHEN o.isPositive = true THEN com.heslin.postopia.enums.OpinionStatus.POSITIVE
                            ELSE com.heslin.postopia.enums.OpinionStatus.NEGATIVE
                            END, c.positiveCount, c.negativeCount, c.isPined) from Comment c
                                   JOIN c.user u
                                   LEFT JOIN CommentOpinion o on o.user.id = :uid and o.comment.id = c.id
                                   where c.post.id = :pid and c.parent IS NULL
                                    ORDER BY c.isPined DESC, c.createdAt ASC
            """)
    Page<CommentInfo> findByPostId(@Param("pid") Long postId, @Param("uid") Long userId, Pageable pageable);


    @Query(value = """

            WITH RECURSIVE comment_tree AS (
                   -- 初始查询：选择指定父评论的直接子评论
                   SELECT
                       c.id,
                       c.parent_id,
                       c.content,
                       c.created_at,
                       u.username,
                       u.nickname,
                       u.avatar,
                       CASE
                           WHEN o.id IS NULL THEN 'NIL'
                           WHEN o.is_positive = TRUE THEN 'POSITIVE'
                           ELSE 'NEGATIVE'
                       END AS opinion_status,
                       c.positive_count,
                       c.negative_count,
                       c.is_pined
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
                       child.parent_id,
                       child.content,
                       child.created_at,
                       u_child.username,
                       u_child.nickname,
                       u_child.avatar,
                       CASE
                           WHEN o_child.id IS NULL THEN 'NIL'
                           WHEN o_child.is_positive = TRUE THEN 'POSITIVE'
                           ELSE 'NEGATIVE'
                       END AS opinion_status,
                       child.positive_count,
                       child.negative_count,
                       child.is_pined
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
                   parent_id,
                   content,
                   created_at,
                   username,
                   nickname,
                   avatar,
                   opinion_status,
                   positive_count,
                   negative_count,
                   is_pined
               FROM comment_tree
               ORDER BY is_pined DESC, created_at ASC;
    """, nativeQuery = true)
    List<Object[]> findChildrenByCommentIds(@Param("cids") List<Long> commentIds, @Param("uid") Long userId);

    @Query("""
    select new com.heslin.postopia.dto.comment.CommentSummary(
        c.id, s.name, p.id, p.subject,SUBSTRING(c.content, 1, 100),
                pu.username,
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

//    @Query("""
//                select new com.heslin.postopia.dto.comment.CommentSummary(
//                c.id, s.name, p.id, p.subject, SUBSTRING(c.content, 1, 100),
//                pu.username,
//                pu.nickname,
//                c.createdAt, c.positiveCount, c.negativeCount)
//                from Comment c
//                JOIN c.post p
//                JOIN p.space s
//                LEFT JOIN c.parent.user pu
//                where c.user.id = :uid
//            """)
//    Page<CommentSummary> findCommentsBySelf(@Param("uid") Long id, Pageable pageable);

    @Query("select new com.heslin.postopia.elasticsearch.dto.SearchedCommentInfo( c.id, c.user.nickname, c.user.avatar, c.positiveCount, c.negativeCount, c.createdAt) from Comment c where c.id in :ids")
    List<SearchedCommentInfo> getCommentInfosInSearch(List<Long> ids);

    @Query("select new com.heslin.postopia.dto.AuthorHint(c.id, c.user.username, SUBSTRING(c.content, 20)) from Comment c where c.id in :ids")
    List<AuthorHint> getAuthorHints(@Param("ids") List<Long> commentIds);

    @Query("select count(*) from Comment c where c.id = :cid and c.isPined = :isPined")
    int checkCommentPinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);

    @Modifying
    @Transactional
    @Query("update Comment c set c.isPined = :isPined where c.id = :cid")
    void updatePinStatus(@Param("cid") Long commentId,@Param("isPined") boolean isPined);

    @Query(value = "select c.content from comments c where c.post_id = :pid LIMIT 500", nativeQuery = true)
    List<String> getCommentContents(@Param("pid") Long postId);
}
