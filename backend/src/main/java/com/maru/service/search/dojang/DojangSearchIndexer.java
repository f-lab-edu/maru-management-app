package com.maru.service.search.dojang;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.repository.tenant.view.DojangSearchView;
import com.maru.service.search.analyzer.AnalyzedToken;
import com.maru.service.search.analyzer.MatchType;
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

import java.util.HashMap;
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

    private static final Map<MatchType, Double> MATCH_TYPE_SCORES = Map.of(
            MatchType.FULLWORD, 5.0,
            MatchType.ENGLISH, 5.0,
            MatchType.NUMBER, 3.0,
            MatchType.CHOSUNG, 2.0,
            MatchType.BIGRAM, 1.0
    );

    private final DojangRepository dojangRepository;
    private final DojangNameAnalyzer nameAnalyzer;
    private final DojangAddressAnalyzer addressAnalyzer;
    private final DojangQueryAnalyzer queryAnalyzer;

    private final Map<String, Set<TokenEntry>> invertedIndex = new ConcurrentHashMap<>();
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

        Set<String> candidateIds = findCandidateIds(queryTokens);
        if (candidateIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<String, Double> scores = calculateScores(candidateIds, queryTokens);

        List<String> sortedIds = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedIds.size());

        if (start >= sortedIds.size()) {
            return new PageImpl<>(List.of(), pageable, sortedIds.size());
        }

        List<DojangSearchDto> content = sortedIds.subList(start, end).stream()
                .map(dataCache::get)
                .filter(Objects::nonNull)
                .toList();

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

    /**
     * @param dojangId 도장 ID
     */
    public void removeFromIndex(String dojangId) {
        DojangSearchDto removed = dataCache.remove(dojangId);
        if (removed == null) {
            return;
        }

        invertedIndex.values().forEach(entries -> entries.removeIf(e -> e.dojangId().equals(dojangId)));
        log.debug("도장 인덱스 제거: {}", removed.name());
    }

    private void indexDojangDto(String id, DojangSearchDto dto) {
        dataCache.put(id, dto);

        indexTokens(id, nameAnalyzer.analyzeDetailed(dto.name()), IndexField.NAME);
        indexTokens(id, addressAnalyzer.analyzeDetailed(dto.address()), IndexField.ADDRESS);

        if (dto.ownerName() != null && !dto.ownerName().isBlank()) {
            for (String token : queryAnalyzer.analyze(dto.ownerName())) {
                if (token != null && !token.isBlank()) {
                    addToIndex(token, new TokenEntry(id, IndexField.OWNER, MatchType.FULLWORD));
                }
            }
        }
    }

    private void indexTokens(String id, Set<AnalyzedToken> analyzedTokens, IndexField field) {
        for (AnalyzedToken at : analyzedTokens) {
            addToIndex(at.token(), new TokenEntry(id, field, at.matchType()));
        }
    }

    private void addToIndex(String token, TokenEntry entry) {
        invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                .add(entry);
    }

    private Set<String> findCandidateIds(Set<String> queryTokens) {
        Set<String> resultIds = null;

        for (String token : queryTokens) {
            Set<TokenEntry> entries = invertedIndex.get(token);
            if (entries == null || entries.isEmpty()) {
                return Set.of();
            }

            Set<String> tokenDojangIds = entries.stream()
                    .map(TokenEntry::dojangId)
                    .collect(Collectors.toSet());

            if (resultIds == null) {
                resultIds = new HashSet<>(tokenDojangIds);
            } else {
                resultIds.retainAll(tokenDojangIds);
            }

            if (resultIds.isEmpty()) {
                return Set.of();
            }
        }

        return resultIds != null ? resultIds : Set.of();
    }

    private Map<String, Double> calculateScores(Set<String> candidateIds, Set<String> queryTokens) {
        int totalDojangs = dataCache.size();
        Map<String, Double> scores = new HashMap<>();

        for (String token : queryTokens) {
            Set<TokenEntry> entries = invertedIndex.get(token);
            if (entries == null) continue;

            long df = entries.stream().map(TokenEntry::dojangId).distinct().count();
            double idf = Math.log((double) totalDojangs / Math.max(df, 1));

            for (TokenEntry entry : entries) {
                if (!candidateIds.contains(entry.dojangId())) continue;

                double fieldBoost = entry.field().getBoost();
                double matchScore = MATCH_TYPE_SCORES.getOrDefault(entry.matchType(), 1.0);

                scores.merge(entry.dojangId(), fieldBoost * matchScore * idf, Double::sum);
            }
        }

        return scores;
    }
}
