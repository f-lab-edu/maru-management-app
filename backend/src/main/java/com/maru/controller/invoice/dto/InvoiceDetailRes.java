package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.Invoice;
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
        int billingYear,
        int billingMonth,
        List<PaymentRes> payments
) {

    public static InvoiceDetailRes from(Invoice invoice, List<PaymentRes> payments) {
        return InvoiceDetailRes.builder()
                .id(invoice.getId())
                .studentId(invoice.getStudent().getId())
                .studentName(invoice.getStudent().getName())
                .amount(invoice.getAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .issueDate(invoice.getIssueDate())
                .note(invoice.getNote())
                .billingYear(invoice.getBillingYear())
                .billingMonth(invoice.getBillingMonth())
                .payments(payments)
                .build();
    }
}
