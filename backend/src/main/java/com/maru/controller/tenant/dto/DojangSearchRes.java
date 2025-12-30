package com.maru.controller.tenant.dto;

import com.maru.service.tenant.search.dto.DojangSearchDto;
import lombok.Builder;

@Builder
public record DojangSearchRes(
        String id,
        String name,
        String address,
        String ownerName
) {
    public static DojangSearchRes from(DojangSearchDto dto) {
        return DojangSearchRes.builder()
                .id(dto.id())
                .name(dto.name())
                .address(dto.address())
                .ownerName(dto.ownerName())
                .build();
    }
}
