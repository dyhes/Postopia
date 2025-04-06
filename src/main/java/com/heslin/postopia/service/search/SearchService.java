package com.heslin.postopia.service.search;

import com.heslin.postopia.elasticsearch.model.SpaceDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<SpaceDoc> searchSpaces(String keyword, Pageable pageable);
}
