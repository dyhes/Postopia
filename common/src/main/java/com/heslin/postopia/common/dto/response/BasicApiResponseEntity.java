package com.heslin.postopia.common.dto.response;

public class BasicApiResponseEntity extends ApiResponseEntity<Object> {
    private static final long serialVersionUID = 1L;
    protected BasicApiResponseEntity(String message, boolean success) {
        super(null, message, success);
    }

    public static BasicApiResponseEntity sucess(String message) {
        return new BasicApiResponseEntity(message, true);
    }

    public static BasicApiResponseEntity fail(String message) {
        return new BasicApiResponseEntity(message, false);
    }
}
