package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.Payment;
import com.maru.domain.invoice.PaymentMethod;
import com.maru.domain.invoice.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentRes(
        String id,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime paidAt,
        LocalDateTime refundedAt
) {

    public static PaymentRes from(Payment payment) {
        return PaymentRes.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .build();
    }
}
