package com.maru.controller.invoice;

import com.maru.controller.invoice.dto.RefundReq;
import com.maru.controller.invoice.dto.RefundRes;
import com.maru.security.CurrentUserId;
import com.maru.service.invoice.RefundService;
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

@Tag(name = "환불")
@Slf4j
@RestController
@RequestMapping("/api/v1/payments/{paymentId}/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    /**
     * 환불 요청
     *
     * @param paymentId 결제 ID
     * @param dojangId 도장 ID
     * @param request 환불 요청 (amount, reason)
     * @param userId 현재 인증된 사용자 ID
     * @return 환불 결과
     */
    @PostMapping
    public ResponseEntity<RefundRes> refund(
            @PathVariable String paymentId,
            @RequestParam String dojangId,
            @Valid @RequestBody RefundReq request,
            @CurrentUserId String userId) {
        RefundRes response = refundService.refund(dojangId, paymentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
