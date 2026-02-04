package com.maru.service.invoice.dto;

import java.math.BigDecimal;

public record TossConfirmReq(String paymentKey, String orderId, BigDecimal amount) {
}
