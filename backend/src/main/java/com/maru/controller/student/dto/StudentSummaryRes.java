package com.maru.controller.student.dto;

import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StudentSummaryRes(
        String id,
        String name,
        LocalDate birth,
        String photoUrl,
        LocalDate enrolledAt,
        StudentStatus status,
        boolean hasEnrollment
) {
    public static StudentSummaryRes from(Student student, boolean hasEnrollment) {
        return StudentSummaryRes.builder()
                .id(student.getId())
                .name(student.getName())
                .birth(student.getBirth())
                .photoUrl(student.getPhotoUrl())
                .enrolledAt(student.getEnrolledAt())
                .status(student.getStatus())
                .hasEnrollment(hasEnrollment)
                .build();
    }
}
