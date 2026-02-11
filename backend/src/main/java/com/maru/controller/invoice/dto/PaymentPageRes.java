package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.YearMonth;

@Builder
public record PaymentPageRes(
        String invoiceId,
        String studentName,
        YearMonth billingYearMonth,
        BigDecimal totalAmount,
        BigDecimal remainingAmount,
        String dojangName,
        String clientKey,
        String orderId
) {
}
