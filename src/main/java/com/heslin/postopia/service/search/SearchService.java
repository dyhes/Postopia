package com.heslin.postopia.service.search;

import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.elasticsearch.model.UserDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<SpaceDoc> searchSpaces(String keyword, Pageable pageable);

    Page<UserDoc> searchUsers(String query, Pageable pageable);

    Page<PostDoc> searchPosts(String query, Pageable pageable);

    Page<PostDoc> searchPostsByUser(String query, String userName, Pageable pageable);

    Page<PostDoc> searchPostsBySpace(String query, String spaceName, Pageable pageable);

    Page<CommentDoc> searchComments(String query, Pageable pageable);

    Page<CommentDoc> searchCommentsByUser(String query, String userName, Pageable pageable);

    Page<CommentDoc> searchCommentsBySpace(String query, String spaceName, Pageable pageable);

    Page<CommentDoc> searchCommentsByPost(String query, String postId, Pageable pageable);
}
