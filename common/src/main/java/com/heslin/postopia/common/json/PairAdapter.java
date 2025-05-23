package com.heslin.postopia.common.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PairAdapter<T, U> {
    private T first;
    private U second;

    public PairAdapter() {}

    @JsonCreator
    public PairAdapter(
    @JsonProperty("first") T first,@JsonProperty("second") U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }
}
