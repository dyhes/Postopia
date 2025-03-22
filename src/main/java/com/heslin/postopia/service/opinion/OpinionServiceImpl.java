package com.heslin.postopia.service.opinion;

import com.heslin.postopia.dto.comment.UserOpinionCommentSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.User;
import com.heslin.postopia.model.opinion.Opinion;
import com.heslin.postopia.model.opinion.PostOpinion;
import com.heslin.postopia.repository.OpinionRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OpinionServiceImpl implements OpinionService {
    private final OpinionRepository opinionRepository;

    @Autowired
    public OpinionServiceImpl(OpinionRepository opinionRepository) {
        this.opinionRepository = opinionRepository;
    }

    @Override
    public void upsertOpinion(Opinion opinion) {
        var tp = opinion.getFields();
        var updateAt = Instant.now();
        switch (opinion.getDiscriminator()) {
            case "COMMENT":
                opinionRepository.upsertCommentOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getLeft());
                break;
            case "POST":
                opinionRepository.upsertPostOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getMiddle());
                break;
            case "VOTE":
                opinionRepository.upsertVoteOpinion(updateAt, opinion.isPositive(), opinion.getUser().getId(), tp.getRight());
                break;
            default:
                throw new IllegalArgumentException("Unknown opinion type: " + opinion.getDiscriminator());
        }
    }

    @Override
    public Page<UserOpinionCommentSummary> getCommentOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
        return opinionRepository.getCommentOpinionsByUser(id, statuses, pageable);
    }

    @Override
    public Page<UserOpinionPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
        return opinionRepository.getPostOpinionsByUser(id, statuses, pageable);
    }
}
