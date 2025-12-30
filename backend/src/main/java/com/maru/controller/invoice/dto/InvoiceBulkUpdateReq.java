package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record InvoiceBulkUpdateReq(
        @NotEmpty(message = "수정할 청구서 ID 목록은 필수입니다")
        List<String> invoiceIds,

        @Positive(message = "금액은 0보다 커야 합니다")
        BigDecimal amount,

        LocalDate dueDate,

        @Size(max = 500, message = "비고는 500자 이내여야 합니다")
        String note
) {}
