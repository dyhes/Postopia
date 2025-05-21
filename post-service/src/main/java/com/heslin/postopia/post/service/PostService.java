package com.heslin.postopia.post.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.SpaceOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.common.utils.Utils;
import com.heslin.postopia.opinion.dto.OpinionInfo;
import com.heslin.postopia.opinion.enums.OpinionStatus;
import com.heslin.postopia.post.dto.*;
import com.heslin.postopia.post.feign.OpinionFeign;
import com.heslin.postopia.post.feign.SpaceFeign;
import com.heslin.postopia.post.feign.UserFeign;
import com.heslin.postopia.post.feign.VoteFeign;
import com.heslin.postopia.post.model.Post;
import com.heslin.postopia.post.repository.PostRepository;
import com.heslin.postopia.post.request.CreatePostRequest;
import com.heslin.postopia.post.request.UpdatePostRequest;
import com.heslin.postopia.search.model.PostDoc;
import com.heslin.postopia.user.dto.UserInfo;
import com.heslin.postopia.vote.dto.VoteInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RefreshScope
public class PostService {
    private final PostRepository postRepository;
    private final SpaceFeign spaceFeign;
    private final KafkaService kafkaService;
    private final OpinionFeign opinionFeign;
    private final UserFeign userFeign;
    private final VoteFeign voteFeign;
    @Value("${postopia.post.popular-threshold}")
    private Long popularThreshold;


    @Autowired
    public PostService(PostRepository postRepository, SpaceFeign spaceFeign, KafkaService kafkaService, OpinionFeign opinionFeign, UserFeign userFeign, VoteFeign voteFeign) {
        this.postRepository = postRepository;
        this.spaceFeign = spaceFeign;
        this.kafkaService = kafkaService;
        this.opinionFeign = opinionFeign;
        this.userFeign = userFeign;
        this.voteFeign = voteFeign;
    }

    public void validate(Long userId, Long spaceId) {
        if (!spaceFeign.isEligible(userId, spaceId)) {
            throw new RuntimeException("用户无参与此空间的权限");
        }
    }

    public void deletePost(Long postId, Long spaceId, Long userId) {
        boolean success = postRepository.deletePost(postId);
        if (success) {
            kafkaService.sendToUser(userId, UserOperation.POST_DELETED);
            kafkaService.sendToSpace(spaceId, SpaceOperation.POST_DELETED);
            String stringSpaceId = spaceId.toString();
            //sendToDocDelete中定义了递归删除逻辑
            kafkaService.sendToDocDelete("post", postId.toString(), stringSpaceId);
            kafkaService.sendToPostCascade(postId);
        }
    }

    public boolean checkPostArchiveStatus(Long postId, boolean isArchived) {
        return postRepository.checkPostArchiveStatus(postId, isArchived) == 0;
    }

    public void updateArchiveStatus(Long postId, boolean isArchived) {
        postRepository.updateArchiveStatus(postId, isArchived);
    }

    public List<PostOpinionHint> getOpinionHints(List<Long> list) {
        return postRepository.findPOHByIdIn(list);
    }

    public void updatePost(Long xUserId, UpdatePostRequest request) {
        boolean success = postRepository.updateSubjectAndContent(request.postId(), xUserId, request.subject(), request.content()) > 0;
        if (success) {
            Map<String, Object> update = new HashMap<>();
            update.put("subject", request.subject());
            update.put("content", request.content());
            kafkaService.sendToDocUpdate("post", request.postId().toString(), request.spaceId().toString(), update);
        }
    }

    public Long createPost(Long xUserId, CreatePostRequest request) {
        var post = Post.builder()
        .spaceId(request.spaceId())
        .spaceName(request.spaceName())
        .userId(xUserId)
        .subject(request.subject())
        .content(request.content())
        .isArchived(false)
        .build();
        post = postRepository.save(post);

        kafkaService.sendToSpace(request.spaceId(), SpaceOperation.POST_CREATED);
        kafkaService.sendToUser(xUserId, UserOperation.POST_CREATED);
        kafkaService.sendToDocCreate("post", post.getId().toString(), new PostDoc(post.getId(), post.getSubject(), post.getContent(), request.spaceId().toString(), xUserId.toString()));
        return post.getId();
    }

    public CompletableFuture<List<FeedPostInfo>> getFeedPostInfos(Long xUserId, List<Long> postId, List<FeedPostPart> posts) {
        List<Long> userId = posts.stream().map(FeedPostPart::userId).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, postId);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(userId);
        CompletableFuture<List<VoteInfo>> futureVoteInfo = voteFeign.getCommentVotes(xUserId, postId);
        return CompletableFuture.allOf(futureUserInfo, futureOpinionInfo, futureVoteInfo).thenApply(v -> {
            List<OpinionInfo> opinionInfos = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<VoteInfo> voteInfos = futureVoteInfo.join();
            return Utils.quaMerge(posts,
            opinionInfos, OpinionInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            userInfos, UserInfo::userId, (postPart, mp) -> mp.get(postPart.userId()),
            voteInfos, VoteInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            FeedPostInfo::new);
        });
    }

    public CompletableFuture<List<FeedPostInfo>> getSearchPosts(Long xUserId, List<Long> ids) {
        List<FeedPostPart> posts = postRepository.findFeedPostByIdIn(ids);
        return getFeedPostInfos(xUserId, ids, posts);
    }

    public CompletableFuture<Page<FeedPostInfo>> getPopularPosts(Long xUserId, Pageable pageable) {
        Page<FeedPostPart> postPage = postRepository.findByCommentCountGreaterThan(popularThreshold, pageable);
        List<FeedPostPart> posts = postPage.getContent();
        List<Long> postId = posts.stream().map(FeedPostPart::id).toList();
        return getFeedPostInfos(xUserId, postId, posts).thenApply(postInfos -> {
            return new PageImpl<>(postInfos, pageable, postPage.getTotalElements());
        });
    }

    public CompletableFuture<Page<UserPostInfo>> getUserPosts(Long xUserId, Long queryId, Pageable pageable) {
        Page<FeedPostPart> postPage = postRepository.findByUserId(queryId, pageable);
        List<FeedPostPart> posts = postPage.getContent();
        List<Long> postId = posts.stream().map(FeedPostPart::id).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, postId);
        return futureOpinionInfo.thenApply(opinionInfos -> {
            List<UserPostInfo> content = Utils.biMerge(posts,
            opinionInfos, OpinionInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            UserPostInfo::new);
            return new PageImpl<>(content, pageable, postPage.getTotalElements());
        });
    }

    private CompletableFuture<List<PostInfo>> getPostInfos(Long xUserId, List<PostPart> posts) {
        List<Long> postId = posts.stream().map(PostPart::id).toList();
        List<Long> userIds = posts.stream().map(PostPart::userId).toList();
        CompletableFuture<List<OpinionInfo>> futureOpinionInfo = opinionFeign.getOpinionInfos(xUserId, postId);
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(userIds);
        CompletableFuture<List<VoteInfo>> futureVoteInfo = voteFeign.getCommentVotes(xUserId, postId);
        return CompletableFuture.allOf(futureUserInfo, futureOpinionInfo, futureVoteInfo).thenApply(v -> {
            List<OpinionInfo> opinionInfos = futureOpinionInfo.join();
            List<UserInfo> userInfos = futureUserInfo.join();
            List<VoteInfo> voteInfos = futureVoteInfo.join();
            return Utils.quaMerge(posts,
            opinionInfos, OpinionInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            userInfos, UserInfo::userId, (postPart, mp) -> mp.get(postPart.userId()),
            voteInfos, VoteInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            PostInfo::new);
        });
    }

    public CompletableFuture<PostInfo> getPostInfo(Long xUserId, Long postId) {
        PostPart post = postRepository.findPostPartById(postId);
        if (post == null) {
            return CompletableFuture.completedFuture(null);
        }
        return getPostInfos(xUserId, List.of(post)).thenApply(postInfos -> postInfos.get(0));
    }

    public CompletableFuture<Page<PostInfo>> getSpacePosts(Long xUserId, Long spaceId, Pageable pageable) {
        Page<PostPart> postPage = postRepository.findBySpaceId(spaceId, pageable);
        List<PostPart> posts = postPage.getContent();
        return getPostInfos(xUserId, posts).thenApply(postInfos -> new PageImpl<>(postInfos, pageable, postPage.getTotalElements()));
    }

    public CompletableFuture<Page<OpinionPostInfo>> getUserOpinionedPosts(Long queryId, OpinionStatus opinion, int page, int size, String direction) {
        Page<OpinionInfo> opinionInfos = opinionFeign.getUserPostOpinion(queryId, page, size, direction, opinion);
        List<OpinionInfo> opinions = opinionInfos.getContent();
        List<Long> postId = opinions.stream().map(OpinionInfo::mergeId).toList();
        if (postId.isEmpty()) {
            return CompletableFuture.completedFuture(new PageImpl<>(List.of(), opinionInfos.getPageable(), opinionInfos.getTotalElements()));
        }
        List<FeedPostPart> posts = postRepository.findFeedPostByIdIn(postId);
        List<Long> userId = posts.stream().map(FeedPostPart::userId).toList();
        CompletableFuture<List<UserInfo>> futureUserInfo = userFeign.getUserInfos(userId);
        return futureUserInfo.thenApply(userInfos -> {
            List<OpinionPostInfo> content = Utils.triMerge(posts,
            opinions, OpinionInfo::mergeId, (postPart, mp) -> mp.get(postPart.id()),
            userInfos, UserInfo::userId, (postPart, mp) -> mp.get(postPart.userId()),
            OpinionPostInfo::new);
            return new PageImpl<>(content, opinionInfos.getPageable(), opinionInfos.getTotalElements());
        });
    }

    public List<CommentPostInfo> getCommentPostInfos(List<Long> ids) {
        return postRepository.findCommentPostInfosByIdIn(ids);
    }

    public SummaryPostInfo getSummaryPostInfo(Long postId) {
        return postRepository.findSummaryById(postId);
    }

//    public Page<FeedPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
//        return opinionRepository.getPostOpinionsByUser(id, statuses, pageable);
//    }

//    public String getPostForSummary(Long postId) {
//        Pair<String, String> pair = postRepository.getBasicInfo(postId);
//        List<String> contents = commentService.getCommentContents(postId);
//        contents.add(0, pair.second());
//        IntelligentPost post = new IntelligentPost(pair.first(), contents);
//        try {
//            return objectMapper.writeValueAsString(post);
//        } catch (JsonProcessingException e) {
//            System.out.println("Error serializing post: " + e.getMessage());
//        }
//        return null;
//    }
}
