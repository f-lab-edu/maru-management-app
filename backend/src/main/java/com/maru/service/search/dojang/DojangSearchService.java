package com.maru.service.search.dojang;

import com.maru.domain.tenant.Dojang;
import com.maru.service.search.SearchService;
import com.maru.service.search.dojang.dto.DojangSearchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DojangSearchService implements SearchService<DojangSearchDto> {

    private final DojangSearchIndexer indexer;

    @Override
    public Page<DojangSearchDto> search(String keyword, Pageable pageable) {
        return indexer.search(keyword, pageable);
    }

    /**
     * @param dojang 도장 엔티티
     */
    public void addToIndex(Dojang dojang) {
        indexer.addToIndex(dojang);
    }

    /**
     * 메모리 인덱스 전체 새로고침
     */
    public void refreshIndex() {
        indexer.rebuildIndex();
    }
}
