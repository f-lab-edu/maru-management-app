package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentRecordReq(
        @NotNull(message = "수납 금액은 필수입니다")
        @Positive(message = "수납 금액은 0보다 커야 합니다")
        BigDecimal amount,

        @NotNull(message = "수납 방법은 필수입니다")
        PaymentMethod method,

        @Size(max = 500, message = "비고는 500자 이내여야 합니다")
        String note
) {}
