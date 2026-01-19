package com.maru.controller.enrollment.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnrolledStudentRes(
        String studentId,
        String studentName,
        LocalDateTime enrolledAt
) {}
