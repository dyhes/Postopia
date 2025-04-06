package com.heslin.postopia.dto.response;

import com.heslin.postopia.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

public class PagedApiResponseEntity<T> extends ApiResponseEntity<PageResult<T>> {
    protected PagedApiResponseEntity(Page<T> data) {
        super(new ApiResponse<>(new PageResult<>(data)), HttpStatus.OK);
    }

    public static <T> PagedApiResponseEntity<T> ok(Page<T> data) {
        return new PagedApiResponseEntity<>(data);
    }
}
