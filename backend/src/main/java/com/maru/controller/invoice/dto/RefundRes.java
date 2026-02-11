package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.Refund;
import com.maru.domain.invoice.RefundStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record RefundRes(
        String id,
        BigDecimal amount,
        String reason,
        RefundStatus status,
        LocalDateTime completedAt
) {

    public static RefundRes from(Refund refund) {
        return RefundRes.builder()
                .id(refund.getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .completedAt(refund.getCompletedAt())
                .build();
    }
}
