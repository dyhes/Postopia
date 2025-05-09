package com.heslin.postopia.service.opinion;


import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.FeedPostSummary;
import com.heslin.postopia.jpa.model.opinion.Opinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OpinionService {
    boolean upsertOpinion(Opinion opinion);

    boolean deletePostOpinion(Long postId, Long userId, boolean isPositive);

    boolean deleteCommentOpinion(Long commentId, Long userId, boolean isPositive);

    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable);

    Page<FeedPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable);
}
