package com.heslin.postopia.comment.dto;

import com.heslin.postopia.post.dto.SummaryPostInfo;

import java.util.List;

public record PostSummary(SummaryPostInfo post, List<SummaryCommentInfo> comments) {
}
