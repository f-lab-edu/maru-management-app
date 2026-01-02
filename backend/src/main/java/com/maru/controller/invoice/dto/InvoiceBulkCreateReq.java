package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Builder
public record InvoiceBulkCreateReq(
        List<String> studentIds,

        @NotNull(message = "금액은 필수입니다")
        @Positive(message = "금액은 0보다 커야 합니다")
        BigDecimal defaultAmount,

        @NotNull(message = "납부 마감일은 필수입니다")
        LocalDate dueDate,

        @Size(max = 500, message = "비고는 500자 이내여야 합니다")
        String note,

        YearMonth billingYearMonth
) {}
