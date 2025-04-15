package com.heslin.postopia.dto.user;

public record UserInfo(UserId userId, String username, String nickname, String avatar, String email, boolean showEmail) {

    public UserInfo {
        if (!showEmail) {
            email = null;
        }
    }
}
