package com.heslin.postopia.post.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.SpaceOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.post.dto.PostOpinionHint;
import com.heslin.postopia.post.feign.SpaceFeign;
import com.heslin.postopia.post.model.Post;
import com.heslin.postopia.post.repository.PostRepository;
import com.heslin.postopia.post.request.CreatePostRequest;
import com.heslin.postopia.post.request.UpdatePostRequest;
import com.heslin.postopia.search.model.PostDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final SpaceFeign spaceFeign;
    private final KafkaService kafkaService;

    @Autowired
    public PostService(PostRepository postRepository, SpaceFeign spaceFeign, KafkaService kafkaService) {
        this.postRepository = postRepository;
        this.spaceFeign = spaceFeign;
        this.kafkaService = kafkaService;
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
