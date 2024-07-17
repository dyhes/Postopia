package com.heslin.postopia.exception;

import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.heslin.postopia.dto.response.BasicApiResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = BadRequestException.class)
    public BasicApiResponseEntity badRequestException(BadRequestException e, WebRequest request) {
        return BasicApiResponseEntity.badRequest(e.getMessage());
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public BasicApiResponseEntity noSuchElementException(NoSuchElementException e, WebRequest request) {
        return BasicApiResponseEntity.internalServerError(e.getMessage());
    }

    @ExceptionHandler(value = ForbiddenException.class)
    public BasicApiResponseEntity forbiddenException(ForbiddenException e, WebRequest request) {
        return BasicApiResponseEntity.forbidden(e.getMessage());
    }
}
