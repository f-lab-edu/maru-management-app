package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public record PrepaidPaymentReq(
        @NotNull(message = "원생 ID는 필수입니다")
        String studentId,

        @NotNull(message = "시작 연월은 필수입니다")
        YearMonth startYearMonth,

        @NotNull(message = "종료 연월은 필수입니다")
        YearMonth endYearMonth,

        @NotNull(message = "월 수업료는 필수입니다")
        @Positive(message = "월 수업료는 0보다 커야 합니다")
        BigDecimal monthlyAmount,

        @NotNull(message = "총 결제 금액은 필수입니다")
        @Positive(message = "총 결제 금액은 0보다 커야 합니다")
        BigDecimal totalAmount,

        @NotNull(message = "결제 수단은 필수입니다")
        PaymentMethod paymentMethod,

        LocalDate dueDate,

        String note
) {
}
