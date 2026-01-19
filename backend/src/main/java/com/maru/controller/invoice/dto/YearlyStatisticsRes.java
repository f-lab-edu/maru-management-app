package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record YearlyStatisticsRes(
        int year,
        BigDecimal totalPaidAmount,
        BigDecimal totalUnpaidAmount,
        int paidInvoiceCount,
        int unpaidInvoiceCount,
        int partialInvoiceCount,
        List<MonthlyStatisticsData> monthlyData
) {}
