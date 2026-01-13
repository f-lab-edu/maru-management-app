package com.maru.controller.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record DashboardNotificationRes(
        List<NotificationItem> items,
        boolean hasMore
) {
    @Builder
    public record NotificationItem(
            String type,
            String title,
            String description,
            LocalDateTime occurredAt
    ) {}
}
