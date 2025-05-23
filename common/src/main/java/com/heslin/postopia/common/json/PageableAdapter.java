package com.heslin.postopia.common.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageableAdapter extends PageRequest {
    public PageableAdapter() {
        super(0, 0, Sort.unsorted());
    }

    @JsonCreator
    public PageableAdapter(
    @JsonProperty("pageNumber") int pageNumber,
    @JsonProperty("pageSize") int pageSize,
    @JsonProperty("sort") SortAdapter sort) {
        super(pageNumber, pageSize, Sort.unsorted());
    }
}