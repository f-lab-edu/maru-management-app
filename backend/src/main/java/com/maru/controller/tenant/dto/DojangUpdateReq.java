package com.maru.controller.tenant.dto;

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
        Integer autoInvoiceDay,
        Integer autoInvoiceHour,
        Integer autoAbsenceHour
) {}
