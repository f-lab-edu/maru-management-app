package com.maru.controller.attendance.dto;

import lombok.Builder;

@Builder
public record DailySummaryRes(
        int present,
        int absent,
        int sick,
        int excused
) {
}
