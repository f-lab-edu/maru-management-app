package com.maru.controller.message.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record NotificationDailySummaryRes(
        LocalDate sendDate,
        String messageType,
        String messageTypeLabel,
        long totalCount,
        long acceptedCount,
        long failedCount
) {}
