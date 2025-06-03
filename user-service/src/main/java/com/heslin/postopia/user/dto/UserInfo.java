package com.heslin.postopia.user.dto;

import java.time.Instant;

public record UserInfo(Long userId, String username, String nickname, String avatar, String introduction, Long postCount, Long commentCount, Long credit, Instant createdAt) {
}