package com.heslin.postopia.common.dto.response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {

    protected ApiResponseEntity(T data, String message, boolean success) {
        super(new ApiResponse<>(data, message, success), HttpStatus.OK);
    }

    protected ApiResponseEntity(T data, String message) {
        super(new ApiResponse<>(data, message, true), HttpStatus.OK);
    }

    protected ApiResponseEntity(T data) {
        super(new ApiResponse<>(data, "成功"), HttpStatus.OK);
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
}