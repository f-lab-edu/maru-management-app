package com.maru.controller.user.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MyDojangsRes(
        List<MyDojangRes> dojangs,
        int count
) {
    public static MyDojangsRes from(List<MyDojangRes> dojangs) {
        return MyDojangsRes.builder()
                .dojangs(dojangs)
                .count(dojangs.size())
                .build();
    }
}
