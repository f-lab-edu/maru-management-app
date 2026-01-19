package com.maru.service.search.dojang.dto;

import com.maru.domain.tenant.Dojang;
import com.maru.repository.tenant.view.DojangSearchView;

public record DojangSearchDto(
        String id,
        String name,
        String address,
        String ownerName
) {
    public static DojangSearchDto from(Dojang dojang, String ownerName) {
        return new DojangSearchDto(dojang.getId(), dojang.getName(), dojang.getAddress(), ownerName);
    }

    public static DojangSearchDto from(DojangSearchView view) {
        return new DojangSearchDto(view.getId(), view.getName(), view.getAddress(), view.getOwnerName());
    }
}
