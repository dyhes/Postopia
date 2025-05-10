package com.heslin.postopia.elasticsearch.dto;

public record SearchedUserInfo(String username, String avatar, String introduction, Long postCount, Long commentCount, Long credit) {
}
