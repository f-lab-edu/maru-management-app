package com.maru.controller.tenant;

import com.maru.controller.tenant.dto.DojangSearchRes;
import com.maru.service.search.dojang.DojangSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "도장")
@Slf4j
@RestController
@RequestMapping("/api/v1/dojangs")
@RequiredArgsConstructor
public class DojangController {

    private final DojangSearchService dojangSearchService;

    /**
     * @param userId 현재 인증된 사용자 ID
     * @param keyword 검색어 (도장명/주소/관장명 통합 검색)
     * @param pageable 페이지네이션 정보
     * @return 도장 목록 (페이지)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DojangSearchRes>> searchDojangs(
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : userId") String userId,
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        log.debug("도장 검색 요청: userId={}, keyword={}", userId, keyword);

        Page<DojangSearchRes> results = dojangSearchService.search(keyword, pageable)
                .map(DojangSearchRes::from);

        return ResponseEntity.ok(results);
    }
}
