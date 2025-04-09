package com.heslin.postopia.elasticsearch.dto;

public record RoutedDocUpdate(String routing, String docUpdate) {
}
