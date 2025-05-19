package com.heslin.postopia.user.dto;

public record UserPart(Long userId, String username, String nickname, String avatar, String introduction, Long postCount, Long commentCount, Long credit) {
}