package com.maru.controller.message.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecipientStatusRes(
        String guardianId,
        String guardianName,
        String phone,
        String studentName,
        String messageStatus,
        LocalDateTime sentAt
) {}
