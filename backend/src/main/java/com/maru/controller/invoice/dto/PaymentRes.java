package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.PaymentMethod;
import com.maru.domain.invoice.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentRes(
        Long id,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime paidAt,
        String note
) {}
