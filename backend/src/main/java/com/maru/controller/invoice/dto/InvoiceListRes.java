package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.Invoice;
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
        LocalDate issueDate,
        int billingYear,
        int billingMonth
) {

    public static InvoiceListRes from(Invoice invoice) {
        return InvoiceListRes.builder()
                .id(invoice.getId())
                .studentName(invoice.getStudent().getName())
                .amount(invoice.getAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .issueDate(invoice.getIssueDate())
                .billingYear(invoice.getBillingYear())
                .billingMonth(invoice.getBillingMonth())
                .build();
    }
}
