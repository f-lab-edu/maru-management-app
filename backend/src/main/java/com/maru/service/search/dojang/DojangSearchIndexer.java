package com.maru.service.search.dojang;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.view.DojangSearchView;
import com.maru.service.search.dojang.analyzer.DojangAddressAnalyzer;
import com.maru.service.search.dojang.analyzer.DojangNameAnalyzer;
import com.maru.service.search.dojang.analyzer.DojangQueryAnalyzer;
import com.maru.service.search.dojang.dto.DojangSearchDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DojangSearchIndexer {

    private final DojangRepository dojangRepository;
    private final DojangNameAnalyzer nameAnalyzer;
    private final DojangAddressAnalyzer addressAnalyzer;
    private final DojangQueryAnalyzer queryAnalyzer;

    private final Map<String, Set<String>> invertedIndex = new ConcurrentHashMap<>();
    private final Map<String, DojangSearchDto> dataCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        rebuildIndex();
    }

    /**
     * @param keyword 검색어
     * @param pageable 페이지네이션
     * @return 검색 결과
     */
    public Page<DojangSearchDto> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }

        Set<String> queryTokens = queryAnalyzer.analyze(keyword);
        if (queryTokens.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<String> resultIds = null;

        for (String token : queryTokens) {
            Set<String> matchedIds = invertedIndex.get(token);

            if (matchedIds == null || matchedIds.isEmpty()) {
                return Page.empty(pageable);
            }

            if (resultIds == null) {
                resultIds = new HashSet<>(matchedIds);
            } else {
                resultIds.retainAll(matchedIds);
            }

            if (resultIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        List<String> sortedIds = resultIds.stream()
                .sorted()
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedIds.size());

        if (start >= sortedIds.size()) {
            return new PageImpl<>(List.of(), pageable, sortedIds.size());
        }

        List<DojangSearchDto> content = sortedIds.subList(start, end).stream()
                .map(dataCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, sortedIds.size());
    }

    /**
     * 전체 인덱스 재구축
     */
    public void rebuildIndex() {
        log.info("도장 검색 인덱스 재구축 시작");

        invertedIndex.clear();
        dataCache.clear();

        List<DojangSearchView> views = dojangRepository.findAllActiveForSearch();

        for (DojangSearchView view : views) {
            indexDojangDto(view.getId(), DojangSearchDto.from(view));
        }

        log.info("도장 검색 인덱스 재구축 완료: {} 건, 토큰 수: {}", views.size(), invertedIndex.size());
    }

    /**
     * @param dojang 도장 엔티티
     * @param ownerName 관장 이름
     */
    public void addToIndex(Dojang dojang, String ownerName) {
        indexDojangDto(dojang.getId(), DojangSearchDto.from(dojang, ownerName));
        log.debug("도장 인덱스 추가: {}", dojang.getName());
    }

    private void indexDojangDto(String id, DojangSearchDto dto) {
        dataCache.put(id, dto);

        Set<String> tokens = new HashSet<>();

        tokens.addAll(nameAnalyzer.analyze(dto.name()));
        tokens.addAll(addressAnalyzer.analyze(dto.address()));

        if (dto.ownerName() != null && !dto.ownerName().isBlank()) {
            tokens.addAll(queryAnalyzer.analyze(dto.ownerName()));
        }

        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                    .add(id);
        }
    }
}
