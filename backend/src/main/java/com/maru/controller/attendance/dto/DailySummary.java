package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record DailySummary(
        int present,
        int absent,
        int sick,
        int excused
) {
}
