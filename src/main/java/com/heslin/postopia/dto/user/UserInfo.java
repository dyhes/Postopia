package com.heslin.postopia.dto.user;

public record UserInfo(UserId userId, String username, String avatar, String nickname, String email, boolean showEmail) {

    public UserInfo {
        if (!showEmail) {
            email = null;
        }
    }
}
