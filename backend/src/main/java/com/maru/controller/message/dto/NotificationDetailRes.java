package com.maru.controller.message.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationDetailRes(
        String id,
        String guardianName,
        String title,
        String status,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {}
