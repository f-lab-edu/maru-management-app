package com.maru.controller.invoice.dto;

import lombok.Builder;

@Builder
public record BulkCreateRes(
        int createdCount,
        int skippedCount
) {}
