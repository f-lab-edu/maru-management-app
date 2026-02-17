package com.maru.controller.message.dto;

import lombok.Builder;

@Builder
public record MonthlyUsageRes(
        long totalCount,
        long autoCount,
        long broadcastCount
) {}
