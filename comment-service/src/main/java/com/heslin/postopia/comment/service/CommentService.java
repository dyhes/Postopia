package com.heslin.postopia.comment.service;

import com.heslin.postopia.comment.dto.*;
import com.heslin.postopia.comment.feign.OpinionFeign;
import com.heslin.postopia.comment.feign.PostFeign;
import com.heslin.postopia.comment.feign.UserFeign;
import com.heslin.postopia.comment.feign.VoteFeign;
import com.heslin.postopia.comment.model.Comment;
import com.heslin.postopia.comment.repository.CommentRepository;
import com.heslin.postopia.comment.request.CreateCommentRequest;
import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.PostOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.common.utils.PostopiaFormatter;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.dto.CommentPostInfo;
import com.heslin.postopia.post.dto.SummaryPostInfo;
import com.heslin.postopia.search.model.CommentDoc;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.vote.dto.VoteInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.min;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final KafkaService kafkaService;
    private final OpinionFeign opinionFeign;
    private final UserFeign userFeign;
    private final PostFeign postFeign;
    private final VoteFeign voteFeign;

    @Autowired
    public CommentService(CommentRepository commentRepository, KafkaService kafkaService, OpinionFeign opinionFeign, UserFeign userFeign, PostFeign postFeign, VoteFeign voteFeign) {
        this.commentRepository = commentRepository;
        this.kafkaService = kafkaService;
        this.opinionFeign = opinionFeign;
        this.userFeign = userFeign;
        this.postFeign = postFeign;
        this.voteFeign = voteFeign;
    }

    public List<CommentOpinionHint> getOpinionHints(List<Long> list) {
        return commentRepository.findOpinionHints(list);
    }

    public Long createComment(Long xUserId, String xUsername, CreateCommentRequest request) {
        Comment parent  = request.parentId() != null? Comment.builder().id(request.parentId()).build() : null;
        Comment comment = Comment.builder()
        .spaceId(request.spaceId())
        .postId(request.postId())
        .userId(xUserId)
        .parent(parent)
        .isPined(false)
        .content(request.content())
        .build();
        comment = commentRepository.save(comment);

        kafkaService.sendToDocCreate("comment", comment.getId().toString(),
        new CommentDoc(
            comment.getId(),
            comment.getContent(),
            request.spaceId().toString(),
            request.postId().toString(),
            xUserId.toString()));
        kafkaService.sendToPost(request.postId(), PostOperation.COMMENT_CREATED);
        kafkaService.sendToUser(xUserId, UserOperation.COMMENT_CREATED);
        kafkaService.sendToUser(request.userId(), UserOperation.CREDIT_EARNED);

        String messageContent = PostopiaFormatter.formatUser(xUserId, xUsername) +
        "回复了您：" +
        request.content().substring(0, min(request.content().length(), 20)) +
        "... %s".formatted(PostopiaFormatter.formatComment(request.spaceId(), request.postId(), comment.getId()));
        kafkaService.sendMessage(request.userId(), messageContent);

        return comment.getId();
    }

    public void deleteComment(Long spaceId, Long postId, Long commentId, Long userId) {
        List<DeleteCommentInfo> comments = commentRepository.findByParentRecursive(commentId);
        boolean success = commentRepository.deleteByIdIn(comments.stream().map(DeleteCommentInfo::id).toList()) > 0;
        if (!success) {
            throw new RuntimeException("Failed to delete comment");
        }
        String spaceIdStr = spaceId.toString();
        comments.forEach(comment -> {
            kafkaService.sendToPost(postId, PostOperation.COMMENT_DELETED);
            kafkaService.sendToUser(comment.userId(), UserOperation.COMMENT_DELETED);
            kafkaService.sendToDocDelete("comment", comment.id().toString(), spaceIdStr);
            if (!comment.id().equals(commentId)) {
                kafkaService.sendMessage(comment.userId(), "您的评论：%s 已被递归删除".formatted(comment.content()));
            }
            kafkaService.sendToCommentCascade(comment.id());
        });
    }

    @Transactional
    public void deleteCommentByPostIds(List<Long> list) {
        List<DeleteCommentDetail> comments = commentRepository.findByPostIdIn(list);
        boolean success = commentRepository.deleteByIdIn(comments.stream().map(DeleteCommentDetail::id).toList()) > 0;
        if (!success) {
            throw new RuntimeException("Failed to delete comment");
        }
        comments.forEach(comment -> {
            kafkaService.sendToPost(comment.postId(), PostOperation.COMMENT_DELETED);
            kafkaService.sendToUser(comment.userId(), UserOperation.COMMENT_DELETED);
            kafkaService.sendToDocDelete("comment", comment.id().toString(), comment.spaceId().toString());
            kafkaService.sendMessage(comment.userId(), "您的评论：%s 已被递归删除".formatted(comment.content()));
            kafkaService.sendToCommentCascade(comment.id());
        });
    }

    public boolean checkPinStatus(Long commentId, boolean isPined) {
        return commentRepository.checkCommentPinStatus(commentId, isPined) == 0;
    }

    public void updatePinStatus(Long commentId, boolean isPined) {
        commentRepository.updateCommentPinStatus(commentId, isPined);
    }


    public CompletableFuture<Page<UserCommentInfo>> getUserComments(Long xUserId, Long queryId, Pageable pageable) {
        Page<SpaceCommentPart> commentPage = commentRepository.findByUserId(queryId, pageable);
        List<SpaceCommentPart> comments = commentPage.getContent();
        List<Long> commentId = comments.stream().map(SpaceCommentPart::id).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, commentId);
        return futureOpinionInfo.thenApply(opinionInfos -> {
            List<UserCommentInfo> content = Utils.biMerge(comments,
            opinionInfos, OpinionInfo::mergeId, (commentPart, mp) -> mp.get(commentPart.id()),
            UserCommentInfo::new);
            return new PageImpl<>(content, pageable, commentPage.getTotalElements());
        });
    }

    public CompletableFuture<Page<OpinionCommentInfo>> getUserOpinionedComments(Long queryId, OpinionStatus opinion, int page, int size, String direction) {
        Page<OpinionInfo> opinionInfos = opinionFeign.getUserCommentOpinion(queryId, page, size, direction, opinion);
        List<OpinionInfo> opinions = opinionInfos.getContent();
        List<Long> commentId = opinions.stream().map(OpinionInfo::mergeId).toList();
        if (commentId.isEmpty()) {
            return CompletableFuture.completedFuture(new PageImpl<>(List.of(), opinionInfos.getPageable(), opinionInfos.getTotalElements()));
        }
        List<SpaceCommentPart> comments = commentRepository.findByIdIn(commentId);
        List<Long> userId = comments.stream().map(SpaceCommentPart::userId).toList();
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(userId);
        return futureUserInfo.thenApply(userInfos -> {
            List<OpinionCommentInfo> content = Utils.triMerge(
            comments,
            opinions, OpinionInfo::mergeId, (commentPart, mp) -> mp.get(commentPart.id()),
            userInfos, UserInfo::userId, (commentPart, mp) -> mp.get(commentPart.userId()),
            OpinionCommentInfo::new);
            return new PageImpl<>(content, opinionInfos.getPageable(), opinionInfos.getTotalElements());
        });
    }

    public CompletableFuture<List<SearchCommentInfo>> getSearchComments(Long xUserId, List<Long> ids) {
        List<SearchCommentPart> comments = commentRepository.findSearchByIdIn(ids);
        List<Long> commentId = comments.stream().map(SearchCommentPart::id).toList();
        List<Long> postId = comments.stream().map(SearchCommentPart::postId).toList();
        List<Long> userId = comments.stream().map(SearchCommentPart::userId).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, commentId);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(userId);
        CompletableFuture<List<CommentPostInfo>> futurePostInfos = postFeign.getCommentPostInfos(postId);
        return CompletableFuture.allOf(futurePostInfos, futureUserInfo, futureOpinionInfo).thenApply(v -> {
            List<OpinionInfo> opinions = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<CommentPostInfo> infos = futurePostInfos.join();
            return Utils.quaMerge(comments,
            opinions, OpinionInfo::mergeId, (commentPart, mp) -> mp.get(commentPart.id()),
            userInfos, UserInfo::userId, (commentPart, mp) -> mp.get(commentPart.userId()),
            infos, CommentPostInfo::id, (commentPart, mp) -> mp.get(commentPart.postId()),
            SearchCommentInfo::new);}
            );
    }

    public CompletableFuture<Page<RecursiveComment>> getCommentsByPost(Long xUserId, Long postId, Pageable pageable) {
        Page<CommentPart> commentPage = commentRepository.findByPostIdAndParentIdIsNull(postId, pageable);
        List<CommentPart> tops = commentPage.getContent();
        List<Long> topIds = tops.stream().map(CommentPart::id).toList();
        List<CommentPart> subs = commentRepository.findSubs(topIds);
        List<CommentPart> commentParts = Stream.concat(tops.stream(), subs.stream()).toList();
        List<Long> commentId = commentParts.stream().map(CommentPart::id).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, commentId);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(commentParts.stream().map(CommentPart::userId).toList());
        CompletableFuture<List<VoteInfo>> futureVoteInfo = voteFeign.getCommentVotes(xUserId, commentId);
        return CompletableFuture.allOf(futureOpinionInfo, futureUserInfo, futureVoteInfo).thenApply(v -> {
            List<OpinionInfo> opinions = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<VoteInfo> voteInfos = futureVoteInfo.join();
            List<CommentInfo> commentInfos = Utils.quaMerge(commentParts,
            opinions, OpinionInfo::mergeId, (commentPart, mp) -> mp.get(commentPart.id()),
            userInfos, UserInfo::userId, (commentPart, mp) -> mp.get(commentPart.userId()),
            voteInfos, VoteInfo::mergeId, (commentPart, mp) -> mp.get(commentPart.id()),
            CommentInfo::new);
            List<RecursiveComment> comments = commentInfos.stream().map(RecursiveComment::new).toList();
            Map<Long, RecursiveComment> mp = comments.stream().collect(Collectors.toMap(c->c.getComment().comment().id(), Function.identity()));
            for (RecursiveComment comment : comments) {
                if (comment.getComment().comment().parentId() != null) {
                    RecursiveComment parent = mp.get(comment.getComment().comment().parentId());
                    parent.getChildren().add(comment);
                }
            }
            List<RecursiveComment> res = topIds.stream().map(mp::get).toList();
            return new PageImpl<>(res, pageable, commentPage.getTotalElements());
        });
    }

    public PostSummary getSummaryInfo(Long postId) {
        SummaryPostInfo post = postFeign.getSummaryPostInfo(postId);
        List<SummaryCommentInfo> comments = commentRepository.findSummaryByPostId(postId);
        return new PostSummary(post, comments);
    }
}
