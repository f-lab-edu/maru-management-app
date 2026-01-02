package com.maru.controller.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Builder
public record DivisionRes(
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
) {}
