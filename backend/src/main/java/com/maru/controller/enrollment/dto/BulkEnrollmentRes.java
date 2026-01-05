package com.maru.controller.enrollment.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkEnrollmentRes(
        int enrolledCount,
        int skippedCount,
        List<String> skippedStudentIds
) {}
