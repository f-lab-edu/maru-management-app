package com.maru.controller.student.dto;

import com.maru.domain.student.Student;
import lombok.Builder;

import java.util.List;

@Builder
public record StudentListRes(
        List<StudentSummaryRes> students,
        long totalCount,
        int returnedCount,
        boolean hasMore
) {
    public static StudentListRes from(List<Student> students) {
        List<StudentSummaryRes> summaries = students.stream()
                .map(StudentSummaryRes::from)
                .toList();

        return StudentListRes.builder()
                .students(summaries)
                .totalCount(summaries.size())
                .returnedCount(summaries.size())
                .hasMore(false)
                .build();
    }
}
