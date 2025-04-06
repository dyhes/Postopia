package com.heslin.postopia.service.opinion;


import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.jpa.model.opinion.Opinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OpinionService {
    boolean upsertOpinion(Opinion opinion);

    Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable);

    Page<UserOpinionPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable);
}
