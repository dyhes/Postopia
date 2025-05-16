package com.heslin.postopia.search.controller;

import com.heslin.postopia.common.dto.response.PagedApiResponseEntity;
import com.heslin.postopia.search.model.CommentDoc;
import com.heslin.postopia.search.model.PostDoc;
import com.heslin.postopia.search.model.SpaceDoc;
import com.heslin.postopia.search.model.UserDoc;
import com.heslin.postopia.search.service.SearchService;
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
        return PagedApiResponseEntity.success(searchService.searchSpaces(query, pageable));
    }

    @GetMapping("post")
    public PagedApiResponseEntity<PostDoc> searchPosts(@RequestParam String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchPosts(query, pageable));
    }

    @GetMapping("space-post")
    public PagedApiResponseEntity<PostDoc> searchPostsBySpace(@RequestParam String query,
                                                       @RequestParam String spaceName,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchPostsBySpace(query, spaceName, pageable));
    }

    @GetMapping("user-post")
    public PagedApiResponseEntity<PostDoc> searchPostsByUser(@RequestParam String query,
                                                       @RequestParam String userName,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchPostsByUser(query, userName, pageable));
    }

    @GetMapping("comment")
    public PagedApiResponseEntity<CommentDoc> searchComments(@RequestParam String query,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchComments(query, pageable));
    }

    @GetMapping("post-comment")
    public PagedApiResponseEntity<CommentDoc> searchCommentsByPost(@RequestParam String query,
                                                                @RequestParam String postId,
                                                                @RequestParam String spaceName,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchCommentsByPost(query, postId, spaceName, pageable));
    }

    @GetMapping("user-comment")
    public PagedApiResponseEntity<CommentDoc> searchCommentsByUser(@RequestParam String query,
                                                                   @RequestParam String userName,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchCommentsByUser(query, userName, pageable));
    }

    @GetMapping("space-comment")
    public PagedApiResponseEntity<CommentDoc> searchCommentsBySpace(@RequestParam String query,
                                                                   @RequestParam String spaceName,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchCommentsBySpace(query, spaceName, pageable));
    }

    @GetMapping("user")
    public PagedApiResponseEntity<UserDoc> searchUsers(@RequestParam String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.success(searchService.searchUsers(query, pageable));
    }

}
