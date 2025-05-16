package com.heslin.postopia.common.dto.response;

import org.springframework.data.domain.Page;

public class PagedApiResponseEntity<T> extends ApiResponseEntity<PageResult<T>> {
    protected PagedApiResponseEntity(Page<T> data) {
        super(new PageResult<>(data));
    }

    public static <T> PagedApiResponseEntity<T> success(Page<T> data) {
        return new PagedApiResponseEntity<>(data);
    }
}
