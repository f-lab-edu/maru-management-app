package com.maru.service.tenant.search;

import com.maru.service.tenant.search.dto.DojangSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchStrategy {

    /**
     * 키워드로 도장 검색
     *
     * @param keyword 검색어 (도장명/주소/관장명)
     * @param pageable 페이지네이션 정보
     * @return 검색된 도장 목록 (페이지)
     */
    Page<DojangSearchDto> search(String keyword, Pageable pageable);
}
