package com.heslin.postopia.controller;

import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import com.heslin.postopia.dto.response.PagedApiResponseEntity;
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
    public PagedApiResponseEntity<SpaceDoc> searchSpace(@RequestParam String query,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size)  {
        Pageable pageable = PageRequest.of(page, size);
        return PagedApiResponseEntity.ok(searchService.searchSpaces(query, pageable));
    }
}
