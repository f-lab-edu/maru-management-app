package com.maru.service.invoice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maru.common.aop.ValidateDojangAccess;
import com.maru.common.exception.BusinessException;
import com.maru.controller.invoice.dto.RefundReq;
import com.maru.controller.invoice.dto.RefundRes;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentStatus;
import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.domain.invoice.Refund;
import com.maru.domain.invoice.exception.PaymentErrorCode;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.domain.invoice.exception.RefundErrorCode;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.PgPaymentDetailRepository;
import com.maru.repository.invoice.RefundRepository;
import com.maru.security.RequirePermission;
import com.maru.security.TenantContextHolder;
import com.maru.service.invoice.dto.TossCancelRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maru.domain.permission.PermissionType;

@Slf4j
@Service
@RequiredArgsConstructor
@ValidateDojangAccess
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PgPaymentDetailRepository pgPaymentDetailRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    /**
     * 환불 처리 (ONSITE: 즉시 완료, PG: 토스 cancel 호출)
     *
     * @param dojangId 도장 ID
     * @param paymentId 결제 ID
     * @param request 환불 요청 (amount, reason)
     * @return 환불 결과
     * @throws BusinessException ALREADY_REFUNDED - 이미 환불됨
     * @throws BusinessException AMOUNT_EXCEEDS_PAYMENT - 금액 초과
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public RefundRes refund(String dojangId, String paymentId, RefundReq request) {
        String tenantId = TenantContextHolder.getTenantId();
        Payment payment = findPayment(paymentId, tenantId, dojangId);
        Invoice invoice = payment.getInvoice();

        return switch (payment.getChannel()) {
            case ONSITE -> processOnsiteRefund(payment, invoice, request);
            case PG -> processPgRefund(payment, invoice, request);
        };
    }

    /**
     * 전액 환불 (기존 PaymentService.cancelPayment 위임용)
     *
     * @param dojangId 도장 ID
     * @param paymentId 결제 ID
     */
    @RequirePermission(PermissionType.PAYMENT_UPDATE)
    @Transactional
    public void refundFull(String dojangId, String paymentId) {
        String tenantId = TenantContextHolder.getTenantId();
        Payment payment = findPayment(paymentId, tenantId, dojangId);
        Invoice invoice = payment.getInvoice();

        RefundReq request = new RefundReq(payment.getAmount(), "전액 환불");

        switch (payment.getChannel()) {
            case ONSITE -> processOnsiteRefund(payment, invoice, request);
            case PG -> processPgRefund(payment, invoice, request);
        }
    }

    private RefundRes processOnsiteRefund(Payment payment, Invoice invoice, RefundReq request) {
        Refund refund = Refund.createOnsite(payment, request.amount(), request.reason());
        refundRepository.save(refund);

        payment.refund();
        invoice.subtractPayment(request.amount());

        log.info("현장 환불 완료: paymentId={}, amount={}", payment.getId(), request.amount());
        return RefundRes.from(refund);
    }

    private RefundRes processPgRefund(Payment payment, Invoice invoice, RefundReq request) {
        PgPaymentDetail pgDetail = pgPaymentDetailRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new BusinessException(PgPaymentErrorCode.PG_DETAIL_NOT_FOUND));

        Refund refund = Refund.createPg(payment, request.amount(), request.reason());
        refundRepository.save(refund);

        invoice.subtractPayment(request.amount());

        try {
            TossCancelRes cancelRes = tossPaymentClient.cancel(
                    pgDetail.getPaymentKey(), request.reason(),
                    request.amount(), refund.getIdempotencyKey());

            refund.complete(cancelRes.transactionKey(), toJson(cancelRes));
            payment.refund();

            log.info("PG 환불 완료: paymentId={}, amount={}", payment.getId(), request.amount());
        } catch (BusinessException e) {
            refund.fail();
            invoice.addPayment(request.amount());

            log.warn("PG 환불 실패: paymentId={}, amount={} - paidAmount 복구",
                    payment.getId(), request.amount(), e);
            throw e;
        }

        return RefundRes.from(refund);
    }

    private Payment findPayment(String paymentId, String tenantId, String dojangId) {
        Payment payment = paymentRepository.findByIdAndTenantIdAndDojangId(paymentId, tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException(RefundErrorCode.ALREADY_REFUNDED);
        }

        return payment;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 직렬화 실패", e);
            return null;
        }
    }
}
