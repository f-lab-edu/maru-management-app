package com.maru.controller.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record RecentStudentRes(
        List<StudentItem> students
) {
    @Builder
    public record StudentItem(
            String id,
            String name,
            LocalDate enrolledAt,
            String status,
            String photoUrl
    ) {}
}
