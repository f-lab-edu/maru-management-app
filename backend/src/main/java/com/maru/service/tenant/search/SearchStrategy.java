package com.maru.service.tenant.search;

import com.maru.service.tenant.search.dto.DojangSearchDto;

import java.util.List;

public interface SearchStrategy {

    /**
     * 키워드로 도장 검색
     *
     * @param keyword 검색어 (도장명/주소/관장명)
     * @return 검색된 도장 목록
     */
    List<DojangSearchDto> search(String keyword);
}
