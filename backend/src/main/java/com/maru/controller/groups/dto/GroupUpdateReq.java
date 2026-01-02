package com.maru.controller.groups.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record GroupUpdateReq(
        @NotBlank(message = "수련반 이름은 필수입니다")
        @Size(max = 50, message = "수련반 이름은 50자 이내여야 합니다")
        String name,

        DayOfWeek dayOfWeek,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {}
