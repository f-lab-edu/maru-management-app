package com.maru.controller.invoice.dto;

import lombok.Builder;

@Builder
public record BulkIssueRes(
        int issuedCount,
        int failedCount
) {}