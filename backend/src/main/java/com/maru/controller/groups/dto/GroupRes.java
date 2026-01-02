package com.maru.controller.groups.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record GroupRes(
        String id,
        String sectionId,
        String sectionName,
        String name,
        Integer displayOrder,
        DayOfWeek dayOfWeek,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        Integer studentCount
) {}
