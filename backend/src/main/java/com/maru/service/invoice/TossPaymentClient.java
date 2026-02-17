package com.maru.service.invoice;

import com.maru.common.exception.BusinessException;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.service.invoice.dto.TossCancelReq;
import com.maru.service.invoice.dto.TossCancelRes;
import com.maru.service.invoice.dto.TossConfirmReq;
import com.maru.service.invoice.dto.TossConfirmRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Slf4j
@Service
public class TossPaymentClient {

    private final RestClient restClient;

    public TossPaymentClient(@Qualifier("tossRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 토스 결제 승인
     *
     * @param paymentKey 토스 결제 키
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @return 토스 승인 응답
     * @throws BusinessException TOSS_CONFIRM_FAILED - 토스 API 호출 실패
     */
    public TossConfirmRes confirm(String paymentKey, String orderId, BigDecimal amount) {
        TossConfirmReq request = new TossConfirmReq(paymentKey, orderId, amount);

        try {
            return restClient.post()
                    .uri("/payments/confirm")
                    .body(request)
                    .retrieve()
                    .body(TossConfirmRes.class);
        } catch (RestClientException e) {
            log.warn("토스 결제 승인 실패: paymentKey={}, orderId={}", paymentKey, orderId, e);
            throw new BusinessException(PgPaymentErrorCode.TOSS_CONFIRM_FAILED);
        }
    }

    /**
     * 토스 결제 취소
     *
     * @param paymentKey 토스 결제 키
     * @param cancelReason 취소 사유
     * @param cancelAmount 취소 금액
     * @param idempotencyKey 멱등키
     * @return 토스 취소 응답
     * @throws BusinessException TOSS_CANCEL_FAILED - 토스 API 호출 실패
     */
    public TossCancelRes cancel(String paymentKey, String cancelReason,
                                BigDecimal cancelAmount, String idempotencyKey) {
        TossCancelReq request = new TossCancelReq(cancelReason, cancelAmount);

        try {
            return restClient.post()
                    .uri("/payments/{paymentKey}/cancel", paymentKey)
                    .header("Idempotency-Key", idempotencyKey)
                    .body(request)
                    .retrieve()
                    .body(TossCancelRes.class);
        } catch (RestClientException e) {
            log.warn("토스 결제 취소 실패: paymentKey={}", paymentKey, e);
            throw new BusinessException(PgPaymentErrorCode.TOSS_CANCEL_FAILED);
        }
    }
}
