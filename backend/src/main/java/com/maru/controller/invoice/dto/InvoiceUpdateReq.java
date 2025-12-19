package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record InvoiceUpdateReq(
        @Positive(message = "금액은 0보다 커야 합니다")
        BigDecimal amount,

        LocalDate dueDate,

        @Size(max = 500, message = "비고는 500자 이내여야 합니다")
        String note
) {}
