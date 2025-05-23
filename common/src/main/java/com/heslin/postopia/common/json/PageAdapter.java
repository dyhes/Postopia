package com.heslin.postopia.common.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter<T> extends PageImpl<T> {
    public PageAdapter() {
        super(new ArrayList<>());
    }

    @JsonCreator
    public PageAdapter(
    @JsonProperty("content") List<T> content,
    @JsonProperty("pageable") PageableAdapter pageable,
    @JsonProperty("totalElements") long total) {
        super(content, pageable, total);
    }
}