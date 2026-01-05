package com.maru.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService<T> {

    /**
     * @param keyword 검색어
     * @param pageable 페이지네이션
     * @return 검색 결과
     */
    Page<T> search(String keyword, Pageable pageable);
}
