package com.heslin.postopia.common.exception;

import com.heslin.postopia.common.dto.response.BasicApiResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public BasicApiResponseEntity handleRuntimeException(RuntimeException e) {
        System.out.println("runtime exception handler");
        System.out.println(e.getMessage());
        // 返回统一错误响应
        return BasicApiResponseEntity.fail(e.getMessage());
    }
}
