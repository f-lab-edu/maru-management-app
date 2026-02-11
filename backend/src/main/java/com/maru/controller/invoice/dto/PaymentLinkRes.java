package com.maru.controller.invoice.dto;

import com.maru.domain.invoice.PaymentLink;

import java.time.LocalDateTime;

public record PaymentLinkRes(
        String token,
        LocalDateTime expiresAt
) {

    public static PaymentLinkRes from(PaymentLink paymentLink) {
        return new PaymentLinkRes(paymentLink.getToken(), paymentLink.getExpiresAt());
    }
}
