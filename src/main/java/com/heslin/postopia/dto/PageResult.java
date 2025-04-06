package com.heslin.postopia.dto;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
public class PageResult<T> {
    private List<T> data;
    private long currentPage;
    private long totalPage;

    public PageResult(Page<T> page) {
        this.data = page.getContent();
        this.currentPage = page.getNumber() + 1;
        this.totalPage = page.getTotalPages();
    }
}