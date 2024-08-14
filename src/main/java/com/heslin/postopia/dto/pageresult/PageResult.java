package com.heslin.postopia.dto.pageresult;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PageResult<T> {
    private List<T> data;
    private long currentPage;
    private long totalPage;
}