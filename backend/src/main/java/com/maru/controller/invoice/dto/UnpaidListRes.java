package com.maru.controller.invoice.dto;

import com.maru.controller.guardian.dto.GuardianInfo;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Builder
public record UnpaidListRes(
        String invoiceId,
        String studentName,
        List<GuardianInfo> guardians,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        LocalDate dueDate,
        int overdueDays,
        YearMonth billingYearMonth
) {}
