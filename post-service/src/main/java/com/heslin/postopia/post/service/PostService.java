package com.heslin.postopia.post.service;

import com.heslin.postopia.common.kafka.KafkaService;
import com.heslin.postopia.common.kafka.enums.SpaceOperation;
import com.heslin.postopia.common.kafka.enums.UserOperation;
import com.heslin.postopia.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final KafkaService kafkaService;

    @Autowired
    public PostService(PostRepository postRepository, KafkaService kafkaService) {
        this.postRepository = postRepository;
        this.kafkaService = kafkaService;
    }

    public void deletePost(Long postId, Long spaceId, Long userId) {
        boolean success = postRepository.deletePost(postId);
        if (success) {
            kafkaService.sendToUser(userId, UserOperation.POST_DELETED);
            kafkaService.sendToSpace(spaceId, SpaceOperation.POST_DELETED);
            String stringSpaceId = spaceId.toString();
            //sendToDocDelete中定义了递归删除逻辑
            kafkaService.sendToDocDelete("post", postId.toString(), stringSpaceId);
            //not impl
            //kafkaService.sendToUser(pair.second(), UserOperation.COMMENT_DELETED);
        }
    }

    public boolean checkPostArchiveStatus(Long postId, boolean isArchived) {
        // not impl
        return false;
    }

    public void updateArchiveStatus(Long postId, boolean isArchived) {
        // not impl
    }

    //
//
//    public Page<FeedPostSummary> getPostOpinionsByUser(Long id, List<Boolean> statuses, Pageable pageable) {
//        return opinionRepository.getPostOpinionsByUser(id, statuses, pageable);
//    }
}
