package com.heslin.postopia.elasticsearch;

import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.elasticsearch.model.UserDoc;
import com.heslin.postopia.elasticsearch.repository.CommentDocRepository;
import com.heslin.postopia.elasticsearch.repository.PostDocRepository;
import com.heslin.postopia.elasticsearch.repository.SpaceDocRepository;
import com.heslin.postopia.elasticsearch.repository.UserDocRepository;
import com.heslin.postopia.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ElasticService implements SearchService {
    private final SpaceDocRepository spaceDocRepository;
    private final PostDocRepository postDocRepository;
    private final UserDocRepository userDocRepository;
    private final CommentDocRepository commentDocRepository;

    @Autowired
    public ElasticService(SpaceDocRepository spaceDocRepository, PostDocRepository postDocRepository, UserDocRepository userDocRepository, CommentDocRepository commentDocRepository) {
        this.spaceDocRepository = spaceDocRepository;
        this.postDocRepository = postDocRepository;
        this.userDocRepository = userDocRepository;
        this.commentDocRepository = commentDocRepository;
    }

    @Override
    public Page<SpaceDoc> searchSpaces(String keyword, Pageable pageable) {
        return spaceDocRepository.matchSpaceDoc(keyword, pageable);
    }

    @Override
    public Page<UserDoc> searchUsers(String query, Pageable pageable) {
        return userDocRepository.matchUserDoc(query, pageable);
    }

    @Override
    public Page<PostDoc> searchPostsByUser(String query, String userName, Pageable pageable) {
        return postDocRepository.matchPostDocByUser(query, userName, pageable);
    }

    @Override
    public Page<PostDoc> searchPostsBySpace(String query, String spaceName, Pageable pageable) {
        System.out.println("searchPostsBySpace: query:" + query + ", spacename: " + spaceName);
        return postDocRepository.matchPostDocBySpace(query, spaceName, pageable);
    }

    @Override
    public Page<PostDoc> searchPosts(String query, Pageable pageable) {
        return postDocRepository.matchPostDoc(query, pageable);
    }

    @Override
    public Page<CommentDoc> searchComments(String query, Pageable pageable) {
        return commentDocRepository.matchCommentDoc(query, pageable);
    }

    @Override
    public Page<CommentDoc> searchCommentsByUser(String query, String userName, Pageable pageable) {
        return commentDocRepository.matchCommentDocByUser(query, userName, pageable);
    }

    @Override
    public Page<CommentDoc> searchCommentsBySpace(String query, String spaceName, Pageable pageable) {
        return commentDocRepository.matchCommentDocBySpace(query, spaceName, pageable);
    }

    @Override
    public Page<CommentDoc> searchCommentsByPost(String query, String postId, String spaceName, Pageable pageable) {
        return commentDocRepository.matchCommentDocByPost(query, postId, spaceName, pageable);
    }
}
