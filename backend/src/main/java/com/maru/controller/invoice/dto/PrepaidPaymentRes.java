package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PrepaidPaymentRes(
        int invoiceCount,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        List<String> invoiceIds
) {
}
