package com.maru.service.search.dojang.dto;

import com.maru.domain.tenant.Dojang;
import lombok.Builder;

@Builder
public record DojangSearchDto(
        String id,
        String name,
        String address,
        String ownerName
) {
    public static DojangSearchDto from(Dojang dojang, String ownerName) {
        return DojangSearchDto.builder()
                .id(dojang.getId())
                .name(dojang.getName())
                .address(dojang.getAddress())
                .ownerName(ownerName)
                .build();
    }
}
