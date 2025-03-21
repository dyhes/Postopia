package com.heslin.postopia.dto.user;

public record UserInfo(String username, String avatar, String nickname, String email, boolean showEmail) {

    public UserInfo {
        if (!showEmail) {
            email = null;
        }
    }
}
