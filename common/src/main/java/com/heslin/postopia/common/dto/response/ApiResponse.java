package com.heslin.postopia.common.dto.response;
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

    public ApiResponse(T data, String message) {
        this.message = message;
        this.data = data;
        this.success = true;
    }

    public ApiResponse(T data, String message, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
    }

}
