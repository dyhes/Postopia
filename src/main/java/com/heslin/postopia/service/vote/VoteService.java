package com.heslin.postopia.service.vote;

import com.heslin.postopia.dto.VoteInfo;
import com.heslin.postopia.exception.BadRequestException;
import com.heslin.postopia.jpa.model.User;

import java.util.List;

public interface VoteService {
    List<VoteInfo> getCommentVotes(List<Long> commentIds);

    List<VoteInfo> getPostVotes(List<Long> ids);

    void upsertVoteOpinion(User user, Long id, boolean isPositive);

    Long deleteCommentVote(User user, Long commentId, Long postId, Long userId, String spaceName, String commentContent, String commentAuthor);

    Long pinCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException;

    Long unPinCommentVote(User user, Long commentId, Long postId, String spaceName, String commentContent, String commentAuthor) throws BadRequestException;

    Long deletePostVote(User user, Long postId, String postSubject, String postAuthor, Long spaceId, String spaceName, Long userId);

    Long unArchivePostVote(User user, Long postId, String postSubject, String postAuthor, String spaceName) throws BadRequestException;

    Long archivePostVote(User user, Long postId, String postSubject, String postAuthor, String spaceName) throws BadRequestException;

    VoteInfo getSpaceVote(Long id);

    Long updateSpaceVote(User user, Long id, String name, Long member, String avatar, String description);

    Long expelSpaceUserVote(User user, Long spaceId, String spaceName, Long member, String username, String reason);

    Long muteSpaceUserVote(User user, Long spaceId, String spaceName, Long member, String username, String reason);
}
