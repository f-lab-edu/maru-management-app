package com.maru.controller.section.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Builder
public record DivisionCreateReq(
        @NotNull(message = "수련부 선택은 필수입니다")
        String sectionId,

        @NotBlank(message = "수련반 이름은 필수입니다")
        @Size(max = 50, message = "수련반 이름은 50자 이내여야 합니다")
        String name,

        Set<DayOfWeek> scheduleDays,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {}
