package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record StudentPaymentHistoryRes(
        Long studentId,
        String studentName,
        BigDecimal totalPaidAmount,
        List<PaymentHistoryItem> payments
) {

    @Builder
    public record PaymentHistoryItem(
            Long paymentId,
            Long invoiceId,
            BigDecimal amount,
            String method,
            String paidAt
    ) {}
}