package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.post.PostDraftDto;
import com.heslin.postopia.dto.post.*;
import com.heslin.postopia.elasticsearch.dto.SearchedPostInfo;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.exception.ResourceNotFoundException;
import com.heslin.postopia.jpa.model.*;
import com.heslin.postopia.jpa.model.opinion.PostOpinion;
import com.heslin.postopia.jpa.repository.PostDraftRepository;
import com.heslin.postopia.jpa.repository.PostRepository;
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
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final CommentService commentService;

    private final OpinionService opinionService;

    private final KafkaService kafkaService;
    private final PostDraftRepository postDraftRepository;
    private final SpaceUserInfoService spaceUserInfoService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CommentService commentService, OpinionService opinionService, KafkaService kafkaService, PostDraftRepository postDraftRepository, SpaceUserInfoService spaceUserInfoService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
        this.postDraftRepository = postDraftRepository;
        this.spaceUserInfoService = spaceUserInfoService;
    }

    @Override
    public Pair<Long, Message> createPost(Space space, User user, String subject, String content) {
        var post = new Post();
        post.setSpace(space);
        post.setUser(user);
        post.setSubject(subject);
        post.setContent(content);
        post.setStatus(PostStatus.PUBLISHED);
        post = postRepository.save(post);

        kafkaService.sendToCreate("post", post.getId().toString(), new PostDoc(post.getId(), post.getSubject(), post.getContent(), space.getName(), user.getUsername()));
        return new Pair<>(post.getId(), new Message("Post created successfully", true));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public void authorize(User user, Long postId) {
        var uid = postRepository.findUserIdById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!uid.equals(user.getId())) {
            throw new ForbiddenException();
        }
    }

    @Override
    public void archivePost(Long id) {
        postRepository.updateStatus(id, PostStatus.ARCHIVED);
    }

    @Override
    public void unarchivedPost(Long id) {
        postRepository.updateStatus(id, PostStatus.PUBLISHED);
    }

    @Override
    public void updatePost(Long id, String subject, String content) {
        postRepository.updateSubjectAndContent(id, subject, content);
    }

    @Override
    public void checkPostStatus(Long id) {
        var status = postRepository.findStatusById(id).orElseThrow(() -> new ForbiddenException("Post not found"));
        if (status != PostStatus.PUBLISHED) {
            throw new ForbiddenException();
        }
    }

    @Override
    public void likePost(Long id,@AuthenticationPrincipal User user) {
        addPostOpinion(id, user, true);
    }

    @Override
    public void disLikePost(Long id,@AuthenticationPrincipal  User user) {
        addPostOpinion(id, user, false);
    }


    private void addPostOpinion(Long id, User user, boolean opinion) {
        PostOpinion postOpinion = new PostOpinion();
        postOpinion.setUser(user);
        postOpinion.setPost(new Post(id));
        postOpinion.setPositive(opinion);
        boolean isInsert = opinionService.upsertOpinion(postOpinion);
        if (isInsert) {
            kafkaService.sendToPost(id, opinion? PostOperation.LIKED : PostOperation.DISLIKED);
        } else {
            kafkaService.sendToPost(id, opinion? PostOperation.SWITCH_TO_LIKE : PostOperation.SWITCH_TO_DISLIKE);
        }
    }

    @Override
    public Comment replyPost(Post post, String content, User user, Space space) {
        return commentService.replyToPost(post, content, user, space);
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
    public Page<PostSummary> getPostsByUser(boolean isSelf, Long queryId, Long selfId, Pageable pageable) {
        if (isSelf) {
            return postRepository.findPostSummariesBySelf(queryId, pageable);
        } else {
            return postRepository.findPostSummariesByUserId(queryId, selfId, pageable);
        }
    }

    @Override
    public Page<UserOpinionPostSummary> getPostOpinionsByUser(Long id, OpinionStatus opinionStatus, Pageable pageable) {
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
}
