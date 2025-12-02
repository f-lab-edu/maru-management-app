package com.maru.service.tenant.search;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.service.tenant.search.dto.DojangSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FullTextSearchStrategy implements SearchStrategy {

    private final DojangRepository dojangRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DojangSearchDto> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        List<Dojang> dojangs = dojangRepository.findByKeywordFullText(keyword.trim());

        return dojangs.stream()
                .map(DojangSearchDto::from)
                .toList();
    }
}
