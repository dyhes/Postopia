package com.heslin.postopia.dto.response;

import com.heslin.postopia.dto.Message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private boolean success;
    private T data;

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.success = true;
    }

    public ApiResponse(T data, Message message) {
        this.message = message.message();
        this.data = data;
        this.success = message.success();
    }
}
