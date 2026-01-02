package com.maru.controller.invoice.dto;

import java.math.BigDecimal;

public record MonthlyStatisticsData(
        int month,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal unpaidAmount,
        int paidCount,
        int unpaidCount,
        int partialCount
) {
    public static MonthlyStatisticsData zero(int month) {
        return new MonthlyStatisticsData(
                month,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0
        );
    }
}
