package com.heslin.postopia.service.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heslin.postopia.dto.AuthorHint;
import com.heslin.postopia.dto.ResMessage;
import com.heslin.postopia.dto.post.PostDraftDto;
import com.heslin.postopia.dto.post.*;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.enums.kafka.SpaceOperation;
import com.heslin.postopia.enums.kafka.UserOperation;
import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.exception.ResourceNotFoundException;
import com.heslin.postopia.jpa.model.*;
import com.heslin.postopia.jpa.model.opinion.PostOpinion;
import com.heslin.postopia.jpa.repository.PostDraftRepository;
import com.heslin.postopia.jpa.repository.PostRepository;
import com.heslin.postopia.redis.RedisService;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.kafka.KafkaService;
import com.heslin.postopia.service.opinion.OpinionService;
import com.heslin.postopia.service.space_user_info.SpaceUserInfoService;
import com.heslin.postopia.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final CommentService commentService;

    private final OpinionService opinionService;

    private final KafkaService kafkaService;
    private final PostDraftRepository postDraftRepository;
    private final SpaceUserInfoService spaceUserInfoService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private Long popularThreshold = 1L;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CommentService commentService, OpinionService opinionService, KafkaService kafkaService, PostDraftRepository postDraftRepository, SpaceUserInfoService spaceUserInfoService, RedisService redisService, ObjectMapper objectMapper) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
        this.postDraftRepository = postDraftRepository;
        this.spaceUserInfoService = spaceUserInfoService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Pair<Long, ResMessage> createPost(Space space, User user, String subject, String content) {
        var post = new Post();
        post.setSpace(space);
        post.setUser(user);
        post.setSubject(subject);
        post.setContent(content);
        post.setArchived(false);
        post = postRepository.save(post);

        kafkaService.sendToSpace(space.getId(), SpaceOperation.POST_CREATED);
        kafkaService.sendToUser(user.getId(), UserOperation.POST_CREATED);
        kafkaService.sendToDocCreate("post", post.getId().toString(), new PostDoc(post.getId(), post.getSubject(), post.getContent(), space.getName(), user.getUsername()));
        return new Pair<>(post.getId(), new ResMessage("Post created successfully", true));
    }

    @Override
    public void deletePost(Long id, Long spaceId, Long userId, String spaceName){
        List<Pair<Long, Long>> infos = commentService.getDeleteCommentInfosByPost(id);
        boolean success = postRepository.deletePost(id) > 0;
        if (success) {
            kafkaService.sendToUser(userId, UserOperation.POST_DELETED);
            kafkaService.sendToSpace(spaceId, SpaceOperation.POST_DELETED);
            kafkaService.sendToDocDelete("post", id.toString(), spaceName);
            infos.forEach(pair -> {
                kafkaService.sendToDocDelete("comment", pair.first().toString(), spaceName);
                kafkaService.sendToUser(pair.second(), UserOperation.COMMENT_DELETED);
            });
        }
    }

    @Override
    public void validate(User user, String spaceName) {
        Instant muteUntil = spaceUserInfoService.getMutedUntil(spaceName, user.getUsername());
        if (muteUntil != null) {
            if (muteUntil.isAfter(Instant.now())) {
                throw new ForbiddenException(muteUntil.toString());
            }
        }
    }

    @Override
    public boolean updatePost(Long id, Long userId, String spaceName, String subject, String content) {
        boolean success = postRepository.updateSubjectAndContent(id, userId, subject, content) > 0;
        if (success) {
            Map<String, Object> update = new HashMap<>();
            update.put("subject", subject);
            update.put("content", content);
            kafkaService.sendToDocUpdate("post", id.toString(), spaceName, update);
        }
        return success;
    }

    @Override
    public Comment replyPost(Post post, String content, User user, Space space, Long replyUserId, String replyUser) {
        return commentService.replyToPost(post, content, user, space, replyUserId, replyUser);
    }

    @Override
    public PostInfo getPostInfo(Long id, User user) {
        return postRepository.findPostInfoById(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    @Override
    public Page<SpacePostSummary> getPosts(Long id, Pageable pageable, @AuthenticationPrincipal User user) {
        return postRepository.findPostSummariesBySpaceId(id, user.getId(), pageable);
    }

    @Override
    public Page<FeedPostSummary> getPopularPosts(Pageable pageable, User user) {
        return postRepository.findPopularPostSummaries(popularThreshold, user.getId(), pageable);
    }

    @Override
    public Page<PostSummary> getPostsByUser(boolean isSelf, Long queryId, Long selfId, Pageable pageable) {
        // 改为用户可以点赞自己的帖子
        return postRepository.findPostSummariesByUserId(queryId, selfId, pageable);
//        if (isSelf) {
//            return postRepository.findPostSummariesBySelf(queryId, pageable);
//        } else {
//            return postRepository.findPostSummariesByUserId(queryId, selfId, pageable);
//        }
    }

    @Override
    public Page<FeedPostSummary> getPostOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable) {
        List<Boolean> statuses = opinionStatus == OpinionStatus.NIL ? List.of(true, false) : opinionStatus == OpinionStatus.POSITIVE ? List.of(true) : List.of(false);
        return opinionService.getPostOpinionsByUser(id, statuses, pageable);
    }

    @Override
    public boolean draftPost(Space space, User user, String subject, String content, Long id) {
        if (id == null) {
            if (!spaceUserInfoService.isSpaceMember(space.getId(), user.getId())) {
                return false;
            }
            PostDraft postDraft = PostDraft.builder()
            .subject(subject)
            .content(content)
            .user(user)
            .space(space)
            .build();
            postDraftRepository.save(postDraft);
            return true;
        } else {
            return postDraftRepository.updatePostDraft(id, subject, content, user.getId(), Instant.now()) > 0;
        }
    }

    @Override
    public Page<PostDraftDto> getPostDrafts(User user, Pageable pageable) {
        return postDraftRepository.findPostDraftsByUserId(user.getId(), pageable);
    }

    @Override
    public boolean deleteDraft(Long id, Long userId) {
        return postDraftRepository.deletePostDraftById(id, userId) > 0;
    }

    @Override
    public List<SearchedPostInfo> getPostInfosInSearch(List<Long> ids) {
        return postRepository.findPostInfosInSearch(ids);
    }

    @Override
    public void upsertPostOpinion(User user, Long id, Long userId, String spaceName, boolean isPositive) {
        PostOpinion postOpinion = new PostOpinion();
        postOpinion.setUser(user);
        postOpinion.setPost(new Post(id));
        postOpinion.setPositive(isPositive);
        boolean isInsert = opinionService.upsertOpinion(postOpinion);
        if (isInsert) {
            kafkaService.sendToPost(id, isPositive? PostOperation.LIKED : PostOperation.DISLIKED);
            kafkaService.sendToUser(user.getId(), UserOperation.CREDIT_EARNED);
            if (isPositive) {
                kafkaService.sendToUser(userId, UserOperation.CREDIT_EARNED);
            }
        } else {
            kafkaService.sendToPost(id, isPositive? PostOperation.SWITCH_TO_LIKE : PostOperation.SWITCH_TO_DISLIKE);
        }
        redisService.updateOpinionAggregation(spaceName, id, null, user.getUsername(), isPositive);
    }

    @Override
    public boolean deletePostOpinion(User user, Long id, boolean isPositive) {
        boolean success = opinionService.deletePostOpinion(id, user.getId(), isPositive);
        if (success) {
            kafkaService.sendToPost(id, isPositive? PostOperation.CANCEL_LIKE : PostOperation.CANCEL_DISLIKE);
        }
        return success;
    }

    @Override
    public List<AuthorHint> getAuthorHints(List<Long> postIds) {
        return postRepository.getAuthorHints(postIds);
    }

    @Override
    public boolean checkPostArchiveStatus(Long postId, boolean isArchived) {
        return postRepository.checkPostArchiveStatus(postId, isArchived) == 0;
    }

    @Override
    public void updateArchiveStatus(Long postId, boolean isArchived) {
        postRepository.updateArchiveStatus(postId, isArchived);
    }

    @Override
    public String getPostForSummary(Long postId) {
        Pair<String, String> pair = postRepository.getBasicInfo(postId);
        List<String> contents = commentService.getCommentContents(postId);
        contents.add(0, pair.second());
        IntelligentPost post = new IntelligentPost(pair.first(), contents);
        try {
            return objectMapper.writeValueAsString(post);
        } catch (JsonProcessingException e) {
            System.out.println("Error serializing post: " + e.getMessage());
        }
        return null;
    }
}
