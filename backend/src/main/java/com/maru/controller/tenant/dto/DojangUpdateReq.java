package com.maru.controller.tenant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DojangUpdateReq(
        @Size(min = 2, max = 100, message = "도장 이름은 2~100자 사이여야 합니다")
        String name,
        String phone,
        String address,
        Integer defaultTuition,
        Boolean autoInvoiceEnabled,
        @Min(value = 1, message = "청구일은 1~31 사이여야 합니다")
        @Max(value = 31, message = "청구일은 1~31 사이여야 합니다")
        Integer autoInvoiceDay,
        @Min(value = 0, message = "청구 시각은 0~23 사이여야 합니다")
        @Max(value = 23, message = "청구 시각은 0~23 사이여야 합니다")
        Integer autoInvoiceHour,
        @Min(value = 0, message = "결석 처리 시각은 0~23 사이여야 합니다")
        @Max(value = 23, message = "결석 처리 시각은 0~23 사이여야 합니다")
        Integer autoAbsenceHour
) {}
