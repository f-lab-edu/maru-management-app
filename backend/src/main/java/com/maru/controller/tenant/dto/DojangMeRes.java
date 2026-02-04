package com.maru.controller.tenant.dto;

import com.maru.domain.tenant.Dojang;
import com.maru.domain.tenant.DojangSetting;
import lombok.Builder;

@Builder
public record DojangMeRes(
        String id,
        String name,
        String phone,
        String address,
        Integer defaultTuition,
        Boolean autoInvoiceEnabled,
        Integer autoInvoiceDay,
        Integer autoInvoiceHour,
        Integer autoAbsenceHour
) {
    public static DojangMeRes from(Dojang dojang, DojangSetting setting) {
        return DojangMeRes.builder()
                .id(dojang.getId())
                .name(dojang.getName())
                .phone(dojang.getPhone())
                .address(dojang.getAddress())
                .defaultTuition(setting.getDefaultTuition())
                .autoInvoiceEnabled(setting.getAutoInvoiceEnabled())
                .autoInvoiceDay(setting.getAutoInvoiceDay())
                .autoInvoiceHour(setting.getAutoInvoiceHour())
                .autoAbsenceHour(setting.getAutoAbsenceHour())
                .build();
    }
}
