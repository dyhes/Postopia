package com.heslin.postopia.comment.dto;

import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.post.dto.CommentPostInfo;
import com.heslin.postopia.user.dto.UserInfo;

public record SearchCommentInfo(SearchCommentPart comment, OpinionInfo opinion, UserInfo user, CommentPostInfo post) {
}
