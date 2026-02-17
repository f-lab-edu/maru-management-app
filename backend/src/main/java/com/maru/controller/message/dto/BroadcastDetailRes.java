package com.maru.controller.message.dto;

import com.maru.domain.message.MessageChannel;
import com.maru.domain.message.RecipientType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BroadcastDetailRes(
        String id,
        String title,
        String body,
        MessageChannel channel,
        RecipientType recipientType,
        int totalCount,
        int skippedCount,
        long acceptedCount,
        long failedCount,
        long pendingCount,
        String status,
        String sentByUserName,
        LocalDateTime createdAt
) {}
