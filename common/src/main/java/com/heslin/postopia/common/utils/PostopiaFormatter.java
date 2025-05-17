package com.heslin.postopia.common.utils;
import com.heslin.postopia.common.exception.BadRequestException;

public class PostopiaFormatter {
    public static void isValid(String content) {
        if (content.contains(";")) {
            throw new BadRequestException("; is not allowed");
        }
    }

    public static void isValidComment(String content) {
        if (content.contains("POSTOPIA-COMMENT[DELETED]")) {
            throw new BadRequestException("Invalid Content");
        }
    }

    public static String formatUser(Long userId, String username) {
        return String.format(" 用户 postopia-user{%d;@%s} ", userId, username);
    }

    public static String formatSpace(Long spaceId, String spaceName) {
        return String.format(" 空间postopia-space{%d;%s} ", spaceId, spaceName);
    }

    public static String formatPost(String spaceName, Long postId, String content) {
        return String.format(" postopia-post{%s;%d;%s} ", spaceName, postId, content);
    }

    public static String formatPost(String spaceName, Long postId) {
        return formatPost(spaceName, postId, "查看详情");
    }

    public static String formatComment(String spaceName, Long postId, Long commentId, String content) {
        return String.format(" postopia-comment{%s;%d;%d;%s} ", spaceName, postId, commentId, content);
    }

    public static String formatComment(String spaceName, Long postId, Long commentId) {
        return formatComment(spaceName, postId, commentId, "查看详情");
    }
}
