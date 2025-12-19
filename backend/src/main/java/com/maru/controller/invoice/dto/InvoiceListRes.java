package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InvoiceListRes(
        Long id,
        String studentName,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDate issueDate
) {}
