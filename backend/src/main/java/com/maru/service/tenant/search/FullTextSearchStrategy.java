package com.maru.service.tenant.search;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.DojangRepository;
import com.maru.service.tenant.search.dto.DojangSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FullTextSearchStrategy implements SearchStrategy {

    private final DojangRepository dojangRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DojangSearchDto> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }

        List<Dojang> allDojangs = dojangRepository.findByKeywordFullText(keyword.trim());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allDojangs.size());

        if (start >= allDojangs.size()) {
            return new PageImpl<>(List.of(), pageable, allDojangs.size());
        }

        List<DojangSearchDto> content = allDojangs.subList(start, end).stream()
                .map(DojangSearchDto::from)
                .toList();

        return new PageImpl<>(content, pageable, allDojangs.size());
    }
}
