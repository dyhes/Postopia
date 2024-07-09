package com.heslin.postopia.model.response;

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
}
