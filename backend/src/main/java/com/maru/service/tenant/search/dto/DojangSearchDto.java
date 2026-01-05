package com.maru.service.tenant.search.dto;

import com.maru.domain.tenant.Dojang;
import lombok.Builder;

@Builder
public record DojangSearchDto(
        String id,
        String name,
        String address,
        String ownerName
) {
    public static DojangSearchDto from(Dojang dojang) {
        return DojangSearchDto.builder()
                .id(dojang.getId())
                .name(dojang.getName())
                .address(dojang.getAddress())
                .ownerName(dojang.getOwner().getName())
                .build();
    }
}
