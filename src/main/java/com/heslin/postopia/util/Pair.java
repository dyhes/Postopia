package com.heslin.postopia.util;

public record Pair<T, U>(T first, U second) {
    @Override
    public String toString() {
        return first + "." + second;
    }
}