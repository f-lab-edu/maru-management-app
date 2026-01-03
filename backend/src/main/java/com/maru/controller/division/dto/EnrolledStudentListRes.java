package com.maru.controller.division.dto;

import java.util.List;

public record EnrolledStudentListRes(
        List<EnrolledStudentRes> students
) {
    public static EnrolledStudentListRes from(List<EnrolledStudentRes> students) {
        return new EnrolledStudentListRes(students);
    }
}
