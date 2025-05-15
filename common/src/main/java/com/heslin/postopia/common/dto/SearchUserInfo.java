package com.heslin.postopia.common.dto;

public record SearchUserInfo(String username, String avatar, String introduction, Long postCount, Long commentCount, Long credit) {
}