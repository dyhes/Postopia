package com.heslin.postopia.common.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResult<T> {
    private List<T> data;
    private long currentPage;
    private long totalPage;

    public List<T> getData() {
        return data;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public PageResult(Page<T> page) {
        this.data = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPage = page.getTotalPages();
    }
}