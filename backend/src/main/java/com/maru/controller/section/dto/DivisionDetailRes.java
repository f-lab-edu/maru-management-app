package com.maru.controller.section.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maru.domain.section.Division;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Builder
public record DivisionDetailRes(
        String id,
        String sectionId,
        String sectionName,
        String name,
        Integer displayOrder,
        Set<DayOfWeek> scheduleDays,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        Integer studentCount
) {
    public static DivisionDetailRes from(Division division, int studentCount) {
        return DivisionDetailRes.builder()
                .id(division.getId())
                .sectionId(division.getSection().getId())
                .sectionName(division.getSection().getName())
                .name(division.getName())
                .displayOrder(division.getDisplayOrder())
                .scheduleDays(division.getScheduleDays())
                .startTime(division.getStartTime())
                .endTime(division.getEndTime())
                .studentCount(studentCount)
                .build();
    }
}
