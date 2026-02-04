package com.maru.service.invoice.result;

public record AutoInvoiceResult(
    int targetDojangCount,
    int createdCount,
    int skippedCount,
    int failedDojangCount
) {
    public static AutoInvoiceResult of(int targetDojangCount, int createdCount,
                                       int skippedCount, int failedDojangCount) {
        return new AutoInvoiceResult(targetDojangCount, createdCount, skippedCount, failedDojangCount);
    }
}
