package com.maru.controller.student.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record StudentListRes(
        List<StudentSummaryRes> students,
        long totalCount,
        int returnedCount,
        boolean hasMore
) {}
