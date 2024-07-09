package com.heslin.postopia.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.heslin.postopia.model.response.BasicApiResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = BadRequestException.class)
    public BasicApiResponseEntity badRequestException(BadRequestException e, WebRequest request) {
        return BasicApiResponseEntity.badRequest(e.getMessage());
    }
}
