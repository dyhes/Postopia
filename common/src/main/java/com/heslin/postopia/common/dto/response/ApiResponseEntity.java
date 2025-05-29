package com.heslin.postopia.common.dto.response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {
    private static final long serialVersionUID = 1L;
    protected ApiResponseEntity(T data, String message, boolean success) {
        super(new ApiResponse<>(data, message, success), HttpStatus.OK);
    }

    protected ApiResponseEntity(T data, String message) {
        this(data, message, true);
    }

    protected ApiResponseEntity(T data) {
        this(data, "成功", true);
    }

    protected ApiResponseEntity(HttpStatus status) {
        super(new ApiResponse<>(null, "failed", false), status);
    }

    public static <T> ApiResponseEntity<T> res(T data, ResMessage message) {
        return new ApiResponseEntity<>(data, message.message(), message.success());
    }

    public static <T> ApiResponseEntity<T> success(T data) {
        return new ApiResponseEntity<>(data);
    }

    public static <T> ApiResponseEntity<T> success(T data, String message) {
        return new ApiResponseEntity<>(data, message);
    }

    public static <T> ApiResponseEntity<T> fail(String message) {
        return new ApiResponseEntity<>(null, message, false);
    }

    public static <T> ApiResponseEntity<T> fail() {
        return new ApiResponseEntity<>(null, "操作失败", false);
    }

    public static <T> ApiResponseEntity<T> unauthorized() {
        return new ApiResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}