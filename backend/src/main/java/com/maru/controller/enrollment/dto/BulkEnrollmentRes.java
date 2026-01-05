package com.maru.controller.enrollment.dto;

import java.util.List;

public record BulkEnrollmentRes(
        int enrolledCount,
        int skippedCount,
        List<String> skippedStudentIds
) {
    public static BulkEnrollmentRes of(int enrolledCount, List<String> skippedStudentIds) {
        return new BulkEnrollmentRes(enrolledCount, skippedStudentIds.size(), skippedStudentIds);
    }
}
