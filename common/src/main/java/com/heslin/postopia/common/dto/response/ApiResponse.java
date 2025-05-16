package com.heslin.postopia.common.dto.response;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private T data;
    private String message;
    private boolean success;

    public ApiResponse(T data, String message, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
