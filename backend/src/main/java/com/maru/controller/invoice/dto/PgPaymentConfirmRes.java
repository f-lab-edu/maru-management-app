package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.PgPaymentDetail;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PgPaymentConfirmRes(
        String paymentId,
        String paymentKey,
        String orderId,
        BigDecimal amount,
        String pgMethod,
        String receiptUrl
) {

    public static PgPaymentConfirmRes from(PgPaymentDetail detail) {
        return PgPaymentConfirmRes.builder()
                .paymentId(detail.getPayment() != null ? detail.getPayment().getId() : null)
                .paymentKey(detail.getPaymentKey())
                .orderId(detail.getOrderId())
                .amount(detail.getAmount())
                .pgMethod(detail.getPgMethod())
                .receiptUrl(detail.getReceiptUrl())
                .build();
    }
}
