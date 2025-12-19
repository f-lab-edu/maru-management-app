package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentStatisticsRes(
        BigDecimal totalPaidAmount,
        BigDecimal totalUnpaidAmount,
        int paidInvoiceCount,
        int unpaidInvoiceCount,
        int partialInvoiceCount
) {}