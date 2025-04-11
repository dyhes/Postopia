package com.heslin.postopia.dto.response;

import com.heslin.postopia.dto.ResMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {
    protected ApiResponseEntity(ApiResponse<T> body, HttpStatus status) {
        super(body, status);
    }

    protected ApiResponseEntity(T data) {
        super(new ApiResponse<>(data), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(ApiResponse<T> body) {
        return new ApiResponseEntity<>(body, HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, ResMessage resMessage) {
        return new ApiResponseEntity<>(new ApiResponse<>(resMessage.message(), resMessage.success(), data), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, String message) {
        return new ApiResponseEntity<>(new ApiResponse<>(message, true, data), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> ok(T data, String message, boolean success) {
        return new ApiResponseEntity<>(new ApiResponse<>(message, success, data), HttpStatus.OK);
    }
}