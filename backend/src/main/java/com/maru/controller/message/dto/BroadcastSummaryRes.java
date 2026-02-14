package com.maru.controller.message.dto;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.RecipientType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BroadcastSummaryRes(
        String id,
        String title,
        MessageChannel channel,
        RecipientType recipientType,
        int totalCount,
        int skippedCount,
        long acceptedCount,
        long failedCount,
        long pendingCount,
        String status,
        LocalDateTime createdAt
) {}
