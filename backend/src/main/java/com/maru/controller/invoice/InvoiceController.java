package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.*;
import com.maru.domain.invoice.InvoiceStatus;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.InvoiceService;
import com.maru.service.invoice.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

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
        InvoiceDetailRes response = invoiceService.createInvoice(dojangId, request, userId);
        return ResponseEntity.ok(response);
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
        BulkCreateRes response = invoiceService.createBulkInvoices(dojangId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 청구서 목록 조회 API
     *
     * @param dojangId 도장 ID
     * @param status 청구서 상태 필터 (선택)
     * @param userId 현재 인증된 사용자 ID
     * @return 청구서 목록
     */
    @GetMapping
    public ResponseEntity<List<InvoiceListRes>> getInvoices(
            @RequestParam Long dojangId,
            @RequestParam(required = false) InvoiceStatus status,
            @CurrentUserId Long userId) {
        List<InvoiceListRes> response = invoiceService.getInvoices(dojangId, status);
        return ResponseEntity.ok(response);
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
        InvoiceDetailRes response = invoiceService.getInvoice(dojangId, id);
        return ResponseEntity.ok(response);
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
        InvoiceDetailRes response = invoiceService.updateInvoice(dojangId, id, request, userId);
        return ResponseEntity.ok(response);
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
        InvoiceDetailRes response = invoiceService.issueInvoice(dojangId, id, userId);
        return ResponseEntity.ok(response);
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
        InvoiceDetailRes response = invoiceService.voidInvoice(dojangId, id, userId);
        return ResponseEntity.ok(response);
    }


    /**
     * 청구서 복구 API (VOID → DRAFT)
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 복구된 청구서
     */
    @PatchMapping("/{id}/restore")
    public ResponseEntity<InvoiceDetailRes> restoreInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        InvoiceDetailRes response = invoiceService.restoreInvoice(dojangId, id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 청구서 삭제 API (DRAFT 상태만 삭제 가능)
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        invoiceService.deleteInvoice(dojangId, id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 수납 기록 API
     *
     * @param id 청구서 ID
     * @param dojangId 도장 ID
     * @param request 수납 기록 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 청구서 상세 정보 (수납 내역 포함)
     */
    @PostMapping("/{id}/payments")
    public ResponseEntity<InvoiceDetailRes> recordPayment(
            @PathVariable Long id,
            @RequestParam Long dojangId,
            @Valid @RequestBody PaymentRecordReq request,
            @CurrentUserId Long userId) {
        InvoiceDetailRes response = paymentService.recordPayment(dojangId, id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 수납 취소 API (환불 처리)
     *
     * @param invoiceId 청구서 ID
     * @param paymentId 수납 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 수정된 청구서 상세 정보
     */
    @DeleteMapping("/{invoiceId}/payments/{paymentId}")
    public ResponseEntity<InvoiceDetailRes> cancelPayment(
            @PathVariable Long invoiceId,
            @PathVariable Long paymentId,
            @RequestParam Long dojangId,
            @CurrentUserId Long userId) {
        InvoiceDetailRes response = paymentService.cancelPayment(dojangId, invoiceId, paymentId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 청구서 일괄 발행 API (DRAFT → OPEN)
     *
     * @param dojangId 도장 ID
     * @param request 일괄 발행 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 발행 결과
     */
    @PostMapping("/bulk-issue")
    public ResponseEntity<BulkIssueRes> bulkIssueInvoices(
            @RequestParam Long dojangId,
            @Valid @RequestBody BulkIssueReq request,
            @CurrentUserId Long userId) {
        BulkIssueRes response = invoiceService.bulkIssueInvoices(dojangId, request, userId);
        return ResponseEntity.ok(response);
    }


    /**
     * 청구서 일괄 수정 API (DRAFT 상태만 수정 가능)
     *
     * @param dojangId 도장 ID
     * @param request 일괄 수정 요청
     * @param userId 현재 인증된 사용자 ID
     * @return 수정 결과
     */
    @PatchMapping("/bulk-update")
    public ResponseEntity<BulkUpdateRes> bulkUpdateInvoices(
            @RequestParam Long dojangId,
            @Valid @RequestBody InvoiceBulkUpdateReq request,
            @CurrentUserId Long userId) {
        BulkUpdateRes response = invoiceService.bulkUpdateInvoices(dojangId, request, userId);
        return ResponseEntity.ok(response);
    }
}
