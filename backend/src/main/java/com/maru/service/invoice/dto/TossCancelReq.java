package com.maru.service.invoice.dto;

import java.math.BigDecimal;

public record TossCancelReq(String cancelReason, BigDecimal cancelAmount) {
}
