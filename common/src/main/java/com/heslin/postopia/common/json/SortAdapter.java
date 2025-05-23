package com.heslin.postopia.common.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;

import java.util.List;

public class SortAdapter extends Sort {
    public SortAdapter() {
        super(List.of());
    }

    @JsonCreator
    public SortAdapter(
    @JsonProperty("empty") boolean empty,
    @JsonProperty("unsorted") boolean unsorted,
    @JsonProperty("sorted") boolean sorted) {
        super(List.of());
    }
}