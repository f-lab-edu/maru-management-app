package com.maru.controller.student.dto;

import com.maru.domain.student.Student;
import com.maru.domain.student.StudentStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentRes(
        String id,
        String name,
        LocalDate birth,
        String photoUrl,
        String phone,
        LocalDate enrolledAt,
        StudentStatus status,
        List<GuardianRes> guardians
) {
    public static StudentRes from(Student student) {
        return from(student, List.of());
    }

    public static StudentRes from(Student student, List<GuardianRes> guardians) {
        return StudentRes.builder()
                .id(student.getId())
                .name(student.getName())
                .birth(student.getBirth())
                .photoUrl(student.getPhotoUrl())
                .phone(student.getPhone())
                .enrolledAt(student.getEnrolledAt())
                .status(student.getStatus())
                .guardians(guardians)
                .build();
    }
}
