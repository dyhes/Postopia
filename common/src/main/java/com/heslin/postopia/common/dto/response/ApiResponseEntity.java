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
        super(new ApiResponse<>(data, "success"), HttpStatus.OK);
    }

    public static <T> ApiResponseEntity<T> OK(T data) {
        return new ApiResponseEntity<>(data);
    }
}