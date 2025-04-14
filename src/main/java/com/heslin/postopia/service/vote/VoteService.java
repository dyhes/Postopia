package com.heslin.postopia.service.vote;

import com.heslin.postopia.dto.comment.CommentVote;
import com.heslin.postopia.jpa.model.User;

import java.util.List;

public interface VoteService {
    List<CommentVote> getCommentVotes(List<Long> commentIds);

    void upsertVoteOpinion(User user, Long id, boolean isPositive);

    Long deleteCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor);
}
