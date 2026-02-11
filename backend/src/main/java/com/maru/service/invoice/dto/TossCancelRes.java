package com.maru.service.invoice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossCancelRes(
        String transactionKey,
        String cancelReason,
        BigDecimal cancelAmount,
        String canceledAt
) {
}
