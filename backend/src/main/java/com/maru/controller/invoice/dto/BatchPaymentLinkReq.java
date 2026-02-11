package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchPaymentLinkReq(
        @NotEmpty(message = "청구서 ID 목록은 필수입니다")
        List<String> invoiceIds
) {}
