package com.maru.controller.invoice;

import com.maru.domain.invoice.InvoiceStatus;
import com.maru.security.CurrentUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    /**
     * 청구서 생성 API
     *
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 청구서
     */
    @PostMapping
    public ResponseEntity<Void> createInvoice(
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 일괄 청구서 생성 API
     *
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 생성 결과
     */
    @PostMapping("/bulk")
    public ResponseEntity<Void> createBulkInvoices(
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 청구서 목록 조회 API
     *
     * @param dojangId 도장 ID
     * @param status 청구서 상태 필터 (선택)
     * @param pageable 페이징 정보
     * @param userId 현재 인증된 사용자 ID
     * @return 청구서 목록
     */
    @GetMapping
    public ResponseEntity<Void> getInvoices(
            @RequestParam Long dojangId,
            @RequestParam(required = false) InvoiceStatus status,
            Pageable pageable,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 청구서 상세 조회 API
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 청구서 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<Void> getInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 청구서 수정 API (DRAFT 상태만 가능)
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 수정된 청구서
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 청구서 발행 API (DRAFT → OPEN)
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 발행된 청구서
     */
    @PatchMapping("/{id}/issue")
    public ResponseEntity<Void> issueInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 청구서 무효화 API (DRAFT/OPEN → VOID)
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 무효화된 청구서
     */
    @PatchMapping("/{id}/void")
    public ResponseEntity<Void> voidInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 수납 기록 API
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 수납 기록
     */
    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> recordPayment(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
