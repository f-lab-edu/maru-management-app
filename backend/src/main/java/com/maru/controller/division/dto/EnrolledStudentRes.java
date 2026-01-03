package com.maru.controller.division.dto;

import com.maru.domain.enrollment.Enrollment;
import com.maru.domain.student.Student;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnrolledStudentRes(
        String studentId,
        String studentName,
        LocalDateTime enrolledAt
) {
    public static EnrolledStudentRes from(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        return EnrolledStudentRes.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .enrolledAt(enrollment.getCreatedAt())
                .build();
    }
}
