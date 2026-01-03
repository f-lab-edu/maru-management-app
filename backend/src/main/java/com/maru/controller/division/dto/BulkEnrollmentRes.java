package com.maru.controller.division.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkEnrollmentRes(
        int enrolledCount,
        int skippedCount,
        List<String> skippedStudentIds
) {}
