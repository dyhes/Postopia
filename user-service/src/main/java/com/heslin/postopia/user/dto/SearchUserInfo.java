package com.heslin.postopia.user.dto;

import com.heslin.postopia.common.dto.UserId;

public record SearchUserInfo(UserId userId, String username, String nickname, String avatar, String introduction, Long postCount, Long commentCount, Long credit) {
}