package com.heslin.postopia.search.service;
import com.heslin.postopia.search.model.CommentDoc;
import com.heslin.postopia.search.model.PostDoc;
import com.heslin.postopia.search.model.SpaceDoc;
import com.heslin.postopia.search.model.UserDoc;
import com.heslin.postopia.search.repository.CommentDocRepository;
import com.heslin.postopia.search.repository.PostDocRepository;
import com.heslin.postopia.search.repository.SpaceDocRepository;
import com.heslin.postopia.search.repository.UserDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final SpaceDocRepository spaceDocRepository;
    private final PostDocRepository postDocRepository;
    private final UserDocRepository userDocRepository;
    private final CommentDocRepository commentDocRepository;

    @Autowired
    public SearchService(SpaceDocRepository spaceDocRepository, PostDocRepository postDocRepository, UserDocRepository userDocRepository, CommentDocRepository commentDocRepository) {
        this.spaceDocRepository = spaceDocRepository;
        this.postDocRepository = postDocRepository;
        this.userDocRepository = userDocRepository;
        this.commentDocRepository = commentDocRepository;
    }

    public Page<SpaceDoc> searchSpaces(String keyword, Pageable pageable) {
        return spaceDocRepository.matchSpaceDoc(keyword, pageable);
    }

    
    public Page<UserDoc> searchUsers(String query, Pageable pageable) {
        return userDocRepository.matchUserDoc(query, pageable);
    }

    
    public Page<PostDoc> searchPostsByUser(String query, String userId, Pageable pageable) {
        System.out.println("query " + query + "userId " + userId);
        return postDocRepository.matchPostDocByUser(query, userId, pageable);
    }

    
    public Page<PostDoc> searchPostsBySpace(String query, String spaceId, Pageable pageable) {
        System.out.println("searchPostsBySpace: query:" + query + ", spaceId: " + spaceId);
        return postDocRepository.matchPostDocBySpace(query, spaceId, pageable);
    }

    
    public Page<PostDoc> searchPosts(String query, Pageable pageable) {
        System.out.println("here");
        Page<PostDoc> res = postDocRepository.matchPostDoc(query, pageable);
        System.out.println("res");
        System.out.println(res.getContent());
        return res;
    }

    
    public Page<CommentDoc> searchComments(String query, Pageable pageable) {
        return commentDocRepository.matchCommentDoc(query, pageable);
    }

    
    public Page<CommentDoc> searchCommentsByUser(String query, String userId, Pageable pageable) {
        return commentDocRepository.matchCommentDocByUser(query, userId, pageable);
    }

    
    public Page<CommentDoc> searchCommentsBySpace(String query, String spaceId, Pageable pageable) {
        return commentDocRepository.matchCommentDocBySpace(query, spaceId, pageable);
    }

    
    public Page<CommentDoc> searchCommentsByPost(String query, String postId, String spaceId, Pageable pageable) {
        return commentDocRepository.matchCommentDocByPost(query, postId, spaceId, pageable);
    }
}
