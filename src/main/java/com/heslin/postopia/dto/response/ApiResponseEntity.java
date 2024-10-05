package com.heslin.postopia.dto.response;

import com.heslin.postopia.dto.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {
    public ApiResponseEntity(ApiResponse<T> body, HttpStatus status) {
        super(body, status);
    }

    public static <T> ApiResponseEntity<T> ok(ApiResponse<T> body) {
        return new ApiResponseEntity<>(body, HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, Message message) {
        return new ApiResponseEntity<>(new ApiResponse<>(message.message(), message.success(), data), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, String message) {
        return new ApiResponseEntity<>(new ApiResponse<>(message, true, data), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, String message, boolean success) {
        return new ApiResponseEntity<>(new ApiResponse<>(message, success, data), HttpStatus.OK);
    }
}