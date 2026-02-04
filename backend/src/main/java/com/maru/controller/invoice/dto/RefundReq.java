package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RefundReq(
        @NotNull(message = "환불 금액은 필수입니다")
        @Positive(message = "환불 금액은 0보다 커야 합니다")
        BigDecimal amount,

        @NotBlank(message = "환불 사유는 필수입니다")
        @Size(max = 200, message = "환불 사유는 200자 이내여야 합니다")
        String reason
) {
}
