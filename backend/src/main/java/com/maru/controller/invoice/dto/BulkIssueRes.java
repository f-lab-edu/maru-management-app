package com.maru.controller.invoice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkIssueRes(
        int issuedCount,
        int failedCount,
        List<FailedInvoice> failedInvoices
) {
    @Builder
    public record FailedInvoice(
            Long invoiceId,
            String reason
    ) {}
}