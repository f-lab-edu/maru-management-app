package com.maru.service.invoice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossConfirmRes(
        String paymentKey,
        String orderId,
        String status,
        String method,
        BigDecimal totalAmount,
        @JsonProperty("receipt") Receipt receipt,
        @JsonProperty("mId") String mid
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Receipt(String url) {
    }

    public String receiptUrl() {
        return receipt != null ? receipt.url() : null;
    }
}
