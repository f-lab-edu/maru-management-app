package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.security.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
     * @param request 청구서 생성 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 청구서
     */
    @PostMapping
    public ResponseEntity<InvoiceDetailRes> createInvoice(
            @RequestParam Long dojangId,
            @Valid @RequestBody InvoiceCreateReq request,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 일괄 청구서 생성 API
     *
     * @param dojangId 도장 ID
     * @param request 일괄 청구서 생성 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 생성 결과
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateRes> createBulkInvoices(
            @RequestParam Long dojangId,
            @Valid @RequestBody InvoiceBulkCreateReq request,
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
    public ResponseEntity<Page<InvoiceListRes>> getInvoices(
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
    public ResponseEntity<InvoiceDetailRes> getInvoice(
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
     * @param request 청구서 수정 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 수정된 청구서
     */
    @PatchMapping("/{id}")
    public ResponseEntity<InvoiceDetailRes> updateInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @Valid @RequestBody InvoiceUpdateReq request,
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
    public ResponseEntity<InvoiceDetailRes> issueInvoice(
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
    public ResponseEntity<InvoiceDetailRes> voidInvoice(
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
     * @param request 수납 기록 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 수납 기록
     */
    @PostMapping("/{id}/payments")
    public ResponseEntity<PaymentRes> recordPayment(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @Valid @RequestBody PaymentRecordReq request,
            @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
