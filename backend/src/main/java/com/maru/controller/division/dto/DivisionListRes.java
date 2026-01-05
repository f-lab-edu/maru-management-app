package com.maru.controller.division.dto;

import java.util.List;

public record DivisionListRes(
        List<DivisionRes> divisions
) {
    public static DivisionListRes from(List<DivisionRes> divisions) {
        return new DivisionListRes(divisions);
    }
}
