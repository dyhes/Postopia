package com.heslin.postopia.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {
    public ApiResponseEntity(ApiResponse<T> body, HttpStatus status) {
        super(body, status);
    }

    public static <T> ApiResponseEntity<T> ok(ApiResponse<T> body) {
        return new ApiResponseEntity<>(body, HttpStatus.OK);
    }
}