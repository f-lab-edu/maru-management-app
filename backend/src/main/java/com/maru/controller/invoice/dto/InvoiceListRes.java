package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Builder
public record InvoiceListRes(
        String id,
        String studentName,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDate issueDate,
        YearMonth billingYearMonth,
        boolean studentDeleted
) {
}
