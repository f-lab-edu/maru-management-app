package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record StudentPaymentHistoryRes(
        String studentId,
        String studentName,
        BigDecimal totalPaidAmount,
        List<PaymentHistoryItem> payments
) {

    @Builder
    public record PaymentHistoryItem(
            String paymentId,
            String invoiceId,
            Integer billingYear,
            Integer billingMonth,
            BigDecimal amount,
            String method,
            String status,
            String paidAt,
            String refundedAt
    ) {}
}