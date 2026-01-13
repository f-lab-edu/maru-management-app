package com.maru.controller.dashboard.dto;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record DashboardSummaryRes(
        int currentlyAttendingCount,
        int totalStudents,
        int studentsDiff,
        double attendanceRate,
        double attendanceRateDiff,
        BigDecimal monthlyRevenue,
        BigDecimal revenueTarget,
        int activeStudents,
        int pausedStudents
) {}
