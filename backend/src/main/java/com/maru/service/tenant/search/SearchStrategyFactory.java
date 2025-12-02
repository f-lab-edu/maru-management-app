package com.maru.service.tenant.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchStrategyFactory {

    private final MemorySearchStrategy memorySearchStrategy;
    private final LikeSearchStrategy likeSearchStrategy;
    private final FullTextSearchStrategy fullTextSearchStrategy;

    /**
     * 검색 타입에 따른 전략 반환
     *
     * @param type 검색 전략 타입
     * @return 해당 전략 구현체
     */
    public SearchStrategy getStrategy(SearchType type) {
        return switch (type) {
            case MEMORY -> memorySearchStrategy;
            case LIKE -> likeSearchStrategy;
            case FULLTEXT -> fullTextSearchStrategy;
        };
    }
}
