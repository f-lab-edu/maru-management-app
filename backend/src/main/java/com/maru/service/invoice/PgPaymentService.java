package com.maru.service.invoice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maru.common.exception.BusinessException;
import com.maru.controller.invoice.dto.PgPaymentConfirmReq;
import com.maru.controller.invoice.dto.PgPaymentConfirmRes;
import com.maru.domain.invoice.Invoice;
import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentLink;
import com.maru.domain.invoice.PgPaymentDetail;
import com.maru.domain.invoice.exception.InvoiceErrorCode;
import com.maru.domain.invoice.exception.PgPaymentErrorCode;
import com.maru.repository.invoice.InvoiceRepository;
import com.maru.repository.invoice.PaymentLinkRepository;
import com.maru.repository.invoice.PaymentRepository;
import com.maru.repository.invoice.PgPaymentDetailRepository;
import com.maru.service.invoice.dto.TossConfirmRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgPaymentService {

    private final PgPaymentDetailRepository pgPaymentDetailRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentLinkRepository paymentLinkRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PgPaymentPendingSaver pendingSaver;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    /**
     * PG 결제 승인 (PENDING 선저장 -> 잔액 검증 -> 토스 승인)
     *
     * @param token 결제 링크 토큰
     * @param request 결제 승인 요청 (paymentKey, orderId, amount)
     * @return 결제 승인 결과
     * @throws BusinessException PAYMENT_LINK_EXPIRED - 만료된 결제 링크
     * @throws BusinessException AMOUNT_EXCEEDS_REMAINING - 잔액 초과
     * @throws BusinessException TOSS_CONFIRM_FAILED - 토스 승인 실패
     */
    @Transactional
    public PgPaymentConfirmRes confirmPayment(String token, PgPaymentConfirmReq request) {
        PaymentLink paymentLink = validatePaymentLink(token);

        Optional<PgPaymentConfirmRes> idempotentResult =
                pgPaymentDetailRepository.findByPaymentKey(request.paymentKey())
                        .map(this::handleExistingDetail);

        if (idempotentResult.isPresent()) {
            return idempotentResult.get();
        }

        PgPaymentDetail pendingDetail = savePendingDetail(paymentLink, request);
        return processConfirm(paymentLink, pendingDetail, request);
    }

    private PaymentLink validatePaymentLink(String token) {
        PaymentLink paymentLink = paymentLinkRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(PgPaymentErrorCode.PAYMENT_LINK_NOT_FOUND));
        paymentLink.validateUsable();
        return paymentLink;
    }

    private PgPaymentConfirmRes handleExistingDetail(PgPaymentDetail existingDetail) {
        return switch (existingDetail.getStatus()) {
            case CONFIRMED -> PgPaymentConfirmRes.from(existingDetail);
            case PENDING -> throw new BusinessException(PgPaymentErrorCode.PAYMENT_IN_PROGRESS);
            case ORPHANED -> throw new BusinessException(PgPaymentErrorCode.PAYMENT_REQUIRES_MANUAL_CHECK);
        };
    }

    private PgPaymentDetail savePendingDetail(PaymentLink paymentLink, PgPaymentConfirmReq request) {
        return pendingSaver.savePending(
                paymentLink.getTenantId(), paymentLink.getDojangId(),
                request.paymentKey(), request.orderId(), request.amount());
    }

    private PgPaymentConfirmRes processConfirm(PaymentLink paymentLink,
                                                PgPaymentDetail pendingDetail,
                                                PgPaymentConfirmReq request) {
        String tenantId = paymentLink.getTenantId();
        String dojangId = paymentLink.getDojangId();

        Invoice invoice = invoiceRepository.findByIdForUpdate(
                        paymentLink.getInvoiceId(), tenantId, dojangId)
                .orElseThrow(() -> new BusinessException(InvoiceErrorCode.NOT_FOUND));

        validateRemainingAmount(invoice, request.amount(), pendingDetail);

        TossConfirmRes tossResponse = callTossConfirmSafely(pendingDetail, request);

        applicationEventPublisher.publishEvent(
                new PgPaymentCompensationEvent(pendingDetail.getId()));

        Payment payment = savePaymentAndUpdateInvoice(invoice, request.amount());
        updatePgDetailToConfirmed(pendingDetail, payment, tossResponse);
        paymentLink.markUsed();

        log.info("PG 결제 승인 완료: paymentKey={}, amount={}", request.paymentKey(), request.amount());
        return PgPaymentConfirmRes.from(pendingDetail);
    }

    private void validateRemainingAmount(Invoice invoice, BigDecimal amount,
                                          PgPaymentDetail pendingDetail) {
        if (invoice.getRemainingAmount().compareTo(amount) < 0) {
            pendingSaver.deletePending(pendingDetail);
            log.warn("PG 결제 거절: 잔액 부족 - paymentKey={}, invoiceId={}, 요청금액={}, 잔액={}",
                    pendingDetail.getPaymentKey(), invoice.getId(), amount, invoice.getRemainingAmount());
            throw new BusinessException(PgPaymentErrorCode.AMOUNT_EXCEEDS_REMAINING);
        }
    }

    private TossConfirmRes callTossConfirmSafely(PgPaymentDetail pendingDetail,
                                                  PgPaymentConfirmReq request) {
        try {
            return tossPaymentClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch (BusinessException e) {
            pendingSaver.deletePending(pendingDetail);
            throw e;
        }
    }

    private Payment savePaymentAndUpdateInvoice(Invoice invoice, BigDecimal amount) {
        Payment payment = Payment.createPg(invoice, amount);
        paymentRepository.save(payment);
        invoice.addPayment(amount);
        return payment;
    }

    private void updatePgDetailToConfirmed(PgPaymentDetail pendingDetail,
                                            Payment payment,
                                            TossConfirmRes tossResponse) {
        pendingDetail.confirm(
                payment,
                toJson(tossResponse),
                tossResponse.method(),
                tossResponse.receiptUrl(),
                null,
                null);
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
