package com.maru.service.tenant.search;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.service.search.dojang.DojangAddressTokenizer;
import com.maru.service.search.dojang.DojangNameTokenizer;
import com.maru.service.search.dojang.DojangQueryTokenizer;
import com.maru.service.tenant.search.dto.DojangSearchDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemorySearchStrategy implements SearchStrategy {

    private final DojangRepository dojangRepository;
    private final DojangNameTokenizer nameTokenizer;
    private final DojangAddressTokenizer addressTokenizer;
    private final DojangQueryTokenizer queryTokenizer;

    private final Map<String, Set<String>> invertedIndex = new ConcurrentHashMap<>();
    private final Map<String, DojangSearchDto> dojangData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public Page<DojangSearchDto> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }

        Set<String> queryTokens = queryTokenizer.tokenize(keyword);
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
                .map(dojangData::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, sortedIds.size());
    }

    /**
     * 전체 인덱스 재구축
     */
    public void refresh() {
        log.info("도장 검색 인덱스 재구축 시작");

        invertedIndex.clear();
        dojangData.clear();

        List<Dojang> dojangs = dojangRepository.findAllActiveWithOwner();

        for (Dojang dojang : dojangs) {
            indexDojang(dojang);
        }

        log.info("도장 검색 인덱스 재구축 완료: {} 건, 토큰 수: {}", dojangs.size(), invertedIndex.size());
    }

    /**
     * 단건 도장 추가
     */
    public void addDojang(Dojang dojang) {
        indexDojang(dojang);
        log.debug("도장 인덱스 추가: {}", dojang.getName());
    }

    private void indexDojang(Dojang dojang) {
        DojangSearchDto dto = DojangSearchDto.from(dojang);
        dojangData.put(dojang.getId(), dto);

        Set<String> tokens = new HashSet<>();

        // 도장명 토큰화
        tokens.addAll(nameTokenizer.tokenize(dto.name()));

        // 주소 토큰화
        tokens.addAll(addressTokenizer.tokenize(dto.address()));

        // 관장명 토큰화 (이름 + 초성)
        if (dto.ownerName() != null && !dto.ownerName().isBlank()) {
            tokens.addAll(queryTokenizer.tokenize(dto.ownerName()));
        }

        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                    .add(dojang.getId());
        }
    }
}
