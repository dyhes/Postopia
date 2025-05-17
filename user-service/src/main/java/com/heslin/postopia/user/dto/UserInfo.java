package com.heslin.postopia.user.dto;

import java.time.Instant;

public record UserInfo(Long userId, String username, String nickname, String avatar, Long postCount, Long commentCount, Long credit, String introduction, String email, boolean showEmail, Instant createdAt) {}
