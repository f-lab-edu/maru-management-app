package com.maru.controller.tenant;

import com.maru.controller.tenant.dto.DojangSearchRes;
import com.maru.security.CurrentUserId;
import com.maru.service.tenant.search.SearchStrategyFactory;
import com.maru.service.tenant.search.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/dojangs")
@RequiredArgsConstructor
public class DojangController {

    private final SearchStrategyFactory searchStrategyFactory;

    /**
     * 도장 검색 API
     *
     * @param userId 현재 인증된 사용자 ID
     * @param keyword 검색어 (도장명/주소/관장명 통합 검색)
     * @param strategy 검색 전략 (MEMORY, LIKE, FULLTEXT)
     * @param pageable 페이지네이션 정보
     * @return 도장 목록 (페이지)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DojangSearchRes>> searchDojangs(
//            @CurrentUserId String userId,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : userId") String userId, // TODO: 테스트용. 프로덕션에서는 반드시 삭제할 것.
            @RequestParam String keyword,
            @RequestParam(defaultValue = "MEMORY") SearchType strategy,
            @PageableDefault(size = 10) Pageable pageable) {

        log.debug("도장 검색 요청: userId={}, keyword={}, strategy={}", userId, keyword, strategy);

        Page<DojangSearchRes> results = searchStrategyFactory.getStrategy(strategy)
                .search(keyword, pageable)
                .map(DojangSearchRes::from);

        return ResponseEntity.ok(results);
    }
}
