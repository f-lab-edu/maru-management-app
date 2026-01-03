package com.maru.controller.enrollment.dto;

import java.util.List;

public record EnrolledStudentListRes(
        List<EnrolledStudentRes> students
) {
    public static EnrolledStudentListRes from(List<EnrolledStudentRes> students) {
        return new EnrolledStudentListRes(students);
    }
}
