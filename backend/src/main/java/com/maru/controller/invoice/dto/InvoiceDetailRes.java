package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.InvoiceStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record InvoiceDetailRes(
        Long id,
        Long studentId,
        String studentName,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDate issueDate,
        String note,
        List<PaymentRes> payments
) {}
