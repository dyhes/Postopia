package com.heslin.postopia.common.utils;
import com.heslin.postopia.common.dto.UserId;
import com.heslin.postopia.common.exception.BadRequestException;

public class PostopiaFormatter {
    public static void isValid(String content) {
        if (content.contains(";")) {
            throw new BadRequestException("; is not allowed");
        }
    }

    public static String formatUser(Long userId, String username) {
        return String.format("用户 postopia-user{%d;@%s} ", UserId.masked(userId), username);
    }

    public static String formatSpace(Long spaceId, String spaceName) {
        return String.format("空间 postopia-space{%d;%s} ", spaceId, spaceName);
    }

    public static String formatPost(Long spaceId, Long postId, String content) {
        return String.format(" postopia-post{%d;%d;%s} ", spaceId, postId, content);
    }

    public static String formatPost(Long spaceId, Long postId) {
        return formatPost(spaceId, postId, "查看详情");
    }

    public static String formatComment(Long spaceId, Long postId, Long commentId, String content) {
        return String.format(" postopia-comment{%d;%d;%d;%s} ", spaceId, postId, commentId, content);
    }

    public static String formatComment(Long spaceId, Long postId, Long commentId) {
        return formatComment(spaceId, postId, commentId, "查看详情");
    }
}
