package com.heslin.postopia.service.post;

import com.heslin.postopia.dto.Message;
import com.heslin.postopia.dto.post.PostInfo;
import com.heslin.postopia.dto.post.PostSummary;
import com.heslin.postopia.dto.post.SpacePostSummary;
import com.heslin.postopia.dto.post.UserOpinionPostSummary;
import com.heslin.postopia.enums.OpinionStatus;
import com.heslin.postopia.enums.PostStatus;
import com.heslin.postopia.enums.kafka.PostOperation;
import com.heslin.postopia.exception.ForbiddenException;
import com.heslin.postopia.exception.ResourceNotFoundException;
import com.heslin.postopia.model.Comment;
import com.heslin.postopia.model.Post;
import com.heslin.postopia.model.Space;
import com.heslin.postopia.model.User;
import com.heslin.postopia.model.opinion.PostOpinion;
import com.heslin.postopia.repository.PostRepository;
import com.heslin.postopia.service.comment.CommentService;
import com.heslin.postopia.service.kafka.KafkaService;
import com.heslin.postopia.service.opinion.OpinionService;
import com.heslin.postopia.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final CommentService commentService;

    private final OpinionService opinionService;

    private final KafkaService kafkaService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CommentService commentService, OpinionService opinionService, KafkaService kafkaService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.opinionService = opinionService;
        this.kafkaService = kafkaService;
    }

    @Override
    public Pair<Long, Message> createPost(boolean isDraft, Space space, User user, String subject, String content) {
        var post = new Post();
        post.setSpace(space);
        post.setUser(user);
        post.setSubject(subject);
        post.setContent(content);
        post.setStatus(isDraft ? PostStatus.DRAFT : PostStatus.PUBLISHED);
        post = postRepository.save(post);
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
    public Comment replyPost(Long id, String content, User user) {
        var post = new Post();
        post.setId(id);
        return commentService.replyToPost(post, content, user);
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
}
