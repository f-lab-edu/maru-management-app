package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.BatchPaymentLinkReq;
import com.maru.controller.invoice.dto.PaymentLinkRes;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.PaymentLinkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "결제 링크")
@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class PgPaymentController {

    private final PaymentLinkService paymentLinkService;

    /**
     * 결제 링크 생성 및 SMS 발송
     *
     * @param invoiceId 청구서 ID
     * @param dojangId 도장 ID
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 결제 링크 정보
     */
    @PostMapping("/{invoiceId}/payment-links")
    public ResponseEntity<PaymentLinkRes> createPaymentLink(
            @PathVariable String invoiceId,
            @RequestParam String dojangId,
            @CurrentUserId String userId) {
        PaymentLinkRes response = paymentLinkService.createAndSend(dojangId, invoiceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 링크 일괄 생성 및 SMS 발송
     *
     * @param dojangId 도장 ID
     * @param request 일괄 발송 요청 (청구서 ID 목록)
     * @param userId 현재 인증된 사용자 ID
     * @return 생성된 결제 링크 정보 목록
     */
    @PostMapping("/payment-links/batch")
    public ResponseEntity<List<PaymentLinkRes>> createBatchPaymentLinks(
            @RequestParam String dojangId,
            @Valid @RequestBody BatchPaymentLinkReq request,
            @CurrentUserId String userId) {
        List<PaymentLinkRes> response = paymentLinkService.createAndSendBatch(dojangId, request.invoiceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
