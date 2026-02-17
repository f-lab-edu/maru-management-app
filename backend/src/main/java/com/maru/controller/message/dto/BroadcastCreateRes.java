package com.maru.controller.message.dto;

import lombok.Builder;

@Builder
public record BroadcastCreateRes(
        String broadcastId,
        int totalCount,
        int skippedCount,
        String status
) {
}
