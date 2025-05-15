package com.heslin.postopia.common.dto.response;
import org.springframework.http.HttpStatus;

public class BasicApiResponseEntity extends ApiResponseEntity<EmptyData> {
    protected BasicApiResponseEntity(String message, boolean success) {
        super(new EmptyData(), message, success);
    }

    public static BasicApiResponseEntity OK(String message, boolean success) {
        return new BasicApiResponseEntity(message, success);
    }

    public static BasicApiResponseEntity OK(ResMessage resMessage) {
        return BasicApiResponseEntity.OK(resMessage.message(), resMessage.success());
    }

//    public static BasicApiResponseEntity OK(String message) {
//        return BasicApiResponseEntity.ok(message, true);
//    }
//
//    public static BasicApiResponseEntity OK(boolean success) {
//        return BasicApiResponseEntity.ok(success? "success" : "failed", success);
//    }
}
