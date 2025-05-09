package com.heslin.postopia.dto.response;

import com.heslin.postopia.dto.ResMessage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private boolean success;
    private T data;

    public ApiResponse(T data) {
        this.data = data;
        this.message = "success";
        this.success = true;
    }

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.success = true;
    }

    public ApiResponse(T data, ResMessage resMessage) {
        this.message = resMessage.message();
        this.data = data;
        this.success = resMessage.success();
    }
}
