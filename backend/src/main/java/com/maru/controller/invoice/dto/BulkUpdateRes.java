package com.maru.controller.invoice.dto;

import lombok.Builder;

@Builder
public record BulkUpdateRes(
        int totalCount,
        int updatedCount,
        int skippedCount
) {
    public static BulkUpdateRes of(int totalCount, int updatedCount) {
        return BulkUpdateRes.builder()
                .totalCount(totalCount)
                .updatedCount(updatedCount)
                .skippedCount(totalCount - updatedCount)
                .build();
    }
}
