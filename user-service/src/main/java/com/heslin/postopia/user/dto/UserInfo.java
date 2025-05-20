package com.heslin.postopia.user.dto;

public record UserInfo(Long userId, String username, String nickname, String avatar, String introduction, Long postCount, Long commentCount, Long credit) {
}