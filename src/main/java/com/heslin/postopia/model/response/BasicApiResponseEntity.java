package com.heslin.postopia.model.response;

import org.springframework.http.HttpStatus;

import com.heslin.postopia.dto.Message;

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

    public static BasicApiResponseEntity ok(String message, boolean success) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, success , null), HttpStatus.OK);
    }

    public static BasicApiResponseEntity ok(Message message) {
        return BasicApiResponseEntity.ok(message.message(), message.success());
    }

    public static BasicApiResponseEntity badRequest(String message) {
        return new BasicApiResponseEntity(new ApiResponse<>(message, false , null), HttpStatus.BAD_REQUEST);
    }
}
