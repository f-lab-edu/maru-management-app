package com.maru.controller.invoice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record BulkIssueReq(
        @NotEmpty(message = "발행할 청구서 ID 목록은 필수입니다")
        List<String> invoiceIds
) {}