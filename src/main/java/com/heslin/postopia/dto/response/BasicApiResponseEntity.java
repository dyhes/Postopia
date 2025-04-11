package com.heslin.postopia.dto.response;

import org.springframework.http.HttpStatus;

import com.heslin.postopia.dto.ResMessage;

import lombok.NoArgsConstructor;

@NoArgsConstructor
class EmptyData {}

public class BasicApiResponseEntity extends ApiResponseEntity<EmptyData> {
    public BasicApiResponseEntity(ApiResponse<EmptyData> body, HttpStatus status) {
        super(body, status);
    }

    public static BasicApiResponseEntity ok(String message) {
        return BasicApiResponseEntity.ok(message, true);
    }

    public static BasicApiResponseEntity ok(boolean success) {
        return BasicApiResponseEntity.ok(success? "success" : "failed", success);
    }

    public static BasicApiResponseEntity ok(String message, boolean success) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, success , null), HttpStatus.OK);
    }

    public static BasicApiResponseEntity ok(ResMessage resMessage) {
        return BasicApiResponseEntity.ok(resMessage.message(), resMessage.success());
    }

    public static BasicApiResponseEntity badRequest(String message) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, false , null), HttpStatus.BAD_REQUEST);
    }

    public static BasicApiResponseEntity internalServerError(String message) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, false , null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static BasicApiResponseEntity forbidden(String message) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, false , null), HttpStatus.FORBIDDEN);
    }

    public static BasicApiResponseEntity notFound(String message) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, false , null), HttpStatus.NOT_FOUND);
    }
}
