package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Builder
public record UnpaidListRes(
        String invoiceId,
        String studentName,
        String guardianName,
        String guardianPhone,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        LocalDate dueDate,
        int overdueDays,
        YearMonth billingYearMonth
) {}
