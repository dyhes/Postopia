package com.heslin.postopia.controller;

import com.heslin.postopia.elasticsearch.model.CommentDoc;
import com.heslin.postopia.elasticsearch.model.PostDoc;
import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.elasticsearch.model.UserDoc;
import com.heslin.postopia.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("search")
public class SearchController {
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("space")
    public PagedApiResponseEntity<SpaceDoc> searchSpaces(@RequestParam String query,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchSpaces(query, pageable));
    }

    @GetMapping("post")
    public PagedApiResponseEntity<PostDoc> searchPosts(@RequestParam String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchPosts(query, pageable));
    }

    @GetMapping("postBySpace")
    public PagedApiResponseEntity<PostDoc> searchPostsBySpace(@RequestParam String query,
                                                       @RequestParam String spaceName,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchPostsBySpace(query, spaceName, pageable));
    }

    @GetMapping("postByUser")
    public PagedApiResponseEntity<PostDoc> searchPostsByUser(@RequestParam String query,
                                                       @RequestParam String userName,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchPostsByUser(query, userName, pageable));
    }

    @GetMapping("comment")
    public PagedApiResponseEntity<CommentDoc> searchComments(@RequestParam String query,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchComments(query, pageable));
    }

    @GetMapping("commentByPost")
    public PagedApiResponseEntity<CommentDoc> searchCommentsByPost(@RequestParam String query,
                                                                @RequestParam String postId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchCommentsByPost(query, postId, pageable));
    }

    @GetMapping("commentByUser")
    public PagedApiResponseEntity<CommentDoc> searchCommentsByUser(@RequestParam String query,
                                                                   @RequestParam String userName,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchCommentsByUser(query, userName, pageable));
    }

    @GetMapping("commentBySpace")
    public PagedApiResponseEntity<CommentDoc> searchCommentsBySpace(@RequestParam String query,
                                                                   @RequestParam String spaceName,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchCommentsBySpace(query, spaceName, pageable));
    }

    @GetMapping("user")
    public PagedApiResponseEntity<UserDoc> searchUsers(@RequestParam String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchUsers(query, pageable));
    }

}
