package com.maru.service.tenant.search;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.service.tenant.search.dto.DojangSearchDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemorySearchStrategy implements SearchStrategy {

    private final DojangRepository dojangRepository;
    private final DojangTokenizer tokenizer;

    private final Map<String, Set<Long>> invertedIndex = new ConcurrentHashMap<>();
    private final Map<Long, DojangSearchDto> dojangData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    @Override
    public List<DojangSearchDto> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        Set<String> queryTokens = tokenizer.tokenizeQuery(keyword);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        // 토큰 중 하나라도 매칭되면 결과에 포함 (OR 검색)
        Set<Long> resultIds = new HashSet<>();
        for (String token : queryTokens) {
            Set<Long> matchedIds = invertedIndex.get(token);
            if (matchedIds != null) {
                resultIds.addAll(matchedIds);
            }
        }

        if (resultIds.isEmpty()) {
            return List.of();
        }

        return resultIds.stream()
                .map(dojangData::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        tokens.addAll(tokenizer.tokenizeName(dto.name()));

        // 주소 토큰화
        tokens.addAll(tokenizer.tokenizeAddress(dto.address()));

        // 관장명 토큰화 (이름 + 초성)
        if (dto.ownerName() != null && !dto.ownerName().isBlank()) {
            tokens.add(dto.ownerName());
            tokens.add(tokenizer.extractChosung(dto.ownerName()));
        }

        for (String token : tokens) {
            if (token == null || token.isBlank()) continue;
            invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                    .add(dojang.getId());
        }
    }
}
