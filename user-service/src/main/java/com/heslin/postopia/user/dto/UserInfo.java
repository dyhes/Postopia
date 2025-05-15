package com.heslin.postopia.user.dto;

import com.heslin.postopia.common.dto.UserId;

public record UserInfo(UserId userId, String username, String nickname, String avatar, Long postCount, Long commentCount, Long credit, String introduction, String email, boolean showEmail) {

    public UserInfo {
        if (!showEmail) {
            email = "保密";
        }
    }
}
