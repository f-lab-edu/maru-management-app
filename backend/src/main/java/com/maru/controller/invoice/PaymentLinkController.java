package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.PaymentPageRes;
import com.maru.controller.invoice.dto.PgPaymentConfirmReq;
import com.maru.controller.invoice.dto.PgPaymentConfirmRes;
import com.maru.service.invoice.PaymentLinkService;
import com.maru.service.invoice.PgPaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제 페이지")
@Slf4j
@RestController
@RequestMapping("/api/v1/pay")
@RequiredArgsConstructor
public class PaymentLinkController {

    private final PaymentLinkService paymentLinkService;
    private final PgPaymentService pgPaymentService;

    /**
     * 결제 페이지 정보 조회 (인증 불필요)
     *
     * @param token 결제 링크 토큰
     * @return 결제 페이지에 표시할 정보
     */
    @GetMapping("/{token}")
    public ResponseEntity<PaymentPageRes> getPaymentPage(@PathVariable String token) {
        PaymentPageRes response = paymentLinkService.getPaymentInfo(token);
        return ResponseEntity.ok(response);
    }

    /**
     * PG 결제 승인 (인증 불필요)
     *
     * @param token 결제 링크 토큰
     * @param request 결제 승인 요청 (paymentKey, orderId, amount)
     * @return 결제 승인 결과
     */
    @PostMapping("/{token}/confirm")
    public ResponseEntity<PgPaymentConfirmRes> confirmPayment(
            @PathVariable String token,
            @Valid @RequestBody PgPaymentConfirmReq request) {
        PgPaymentConfirmRes response = pgPaymentService.confirmPayment(token, request);
        return ResponseEntity.ok(response);
    }
}
