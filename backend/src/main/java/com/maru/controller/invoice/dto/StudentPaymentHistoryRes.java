package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;
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
            YearMonth billingYearMonth,
            BigDecimal amount,
            String method,
            String status,
            String paidAt,
            String refundedAt
    ) {}
}
