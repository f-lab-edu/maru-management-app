package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InvoiceCreateReq(
        @NotNull(message = "원생 ID는 필수입니다")
        Long studentId,

        @Positive(message = "금액은 0보다 커야 합니다")
        BigDecimal amount,

        @NotNull(message = "납부 마감일은 필수입니다")
        LocalDate dueDate,

        @Size(max = 500, message = "비고는 500자 이내여야 합니다")
        String note,

        Integer billingYear,

        @Min(value = 1, message = "청구 월은 1 이상이어야 합니다")
        @Max(value = 12, message = "청구 월은 12 이하여야 합니다")
        Integer billingMonth
) {}
