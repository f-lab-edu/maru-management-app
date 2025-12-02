package com.maru.controller.employment;

import com.maru.common.exception.BusinessException;
import com.maru.common.exception.ErrorCode;
import com.maru.security.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
public class EmploymentController {

    /**
     * 승인 요청 API (사범 → 도장)
     *
     * @param userId 현재 인증된 사용자 ID (사범)
     * @param dojangId 승인 요청할 도장 ID
     * @return 생성된 Employment 정보
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestApproval(
            @CurrentUserId Long userId,
            @RequestParam Long dojangId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 대기 중인 승인 요청 목록 조회
     * - 관장: 본인 도장의 대기 중인 승인 요청 목록
     * - 사범: 본인이 보낸 승인 요청 상태
     *
     * @param userId 현재 인증된 사용자 ID
     * @return 대기 중인 Employment 목록
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(@CurrentUserId Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 승인 처리 API (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 승인할 Employment ID
     * @return 승인된 Employment 정보
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @CurrentUserId Long userId,
            @PathVariable Long id) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 거절 처리 API (관장 전용)
     *
     * @param userId 현재 인증된 사용자 ID (관장)
     * @param id 거절할 Employment ID
     * @return 거절된 Employment 정보
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @CurrentUserId Long userId,
            @PathVariable Long id) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * 요청 취소 API (사범 본인 전용)
     * PENDING 상태의 요청만 취소 가능
     *
     * @param userId 현재 인증된 사용자 ID (사범)
     * @param id 취소할 Employment ID
     * @return 취소 결과
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @CurrentUserId Long userId,
            @PathVariable Long id) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
