package com.maru.controller.tenant;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.ErrorCode;
import com.maru.security.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/dojangs")
@RequiredArgsConstructor
public class DojangController {

    /**
     * 도장 검색 API
     *
     * @param userId 현재 인증된 사용자 ID
     * @param keyword 검색어 (도장명/주소/관장명 통합 검색)
     * @param strategy 검색 전략 (MEMORY, LIKE, FULLTEXT)
     * @return 도장 목록
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDojangs(
            @CurrentUserId Long userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "MEMORY") String strategy) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
