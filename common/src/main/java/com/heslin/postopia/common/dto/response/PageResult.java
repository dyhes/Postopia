package com.heslin.postopia.common.dto.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private long currentPage;
    private long totalPage;

    public PageResult(Page<T> page) {
        this.data = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPage = page.getTotalPages();
    }
}