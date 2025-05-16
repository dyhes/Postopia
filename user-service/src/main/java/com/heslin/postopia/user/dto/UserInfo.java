package com.heslin.postopia.user.dto;

import com.heslin.postopia.common.dto.UserId;

import java.time.Instant;

public record UserInfo(UserId userId, String username, String nickname, String avatar, Long postCount, Long commentCount, Long credit, String introduction, String email, boolean showEmail, Instant createdAt) {}
