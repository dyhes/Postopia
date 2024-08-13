package com.heslin.postopia.dto.post;

public record PostInfo(String subject, String content, long positive, long negative, long comment, String username, String nickname, String avatar) {

}
