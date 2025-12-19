package com.maru.service.invoice.dto;

import java.math.BigDecimal;

public record InvoiceStatistics(
    long paidCount,
    long partialCount,
    long unpaidCount,
    BigDecimal totalUnpaidAmount
) {}
